using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.IO;
using Terraria;
using Terraria.ModLoader;
using Mono.Cecil;
using Mono.Cecil.Cil;
using MonoMod.Cil;

namespace TerreAddon
{
    public class TerreAddon : Mod
    {
        private Guid lastWorldUniqueId = Guid.Empty;
        private string lastWorldName = string.Empty;
        private int lastWorldId = 0;
        private bool resetGameMenuAfterDisconnect = false;

        public override void Load()
        {
            IL_MessageBuffer.GetData += HookGetData;
            IL_Netplay.InnerClientLoop += HookInnerClientLoop;
        }

        private void HookInnerClientLoop(ILContext il)
        {
            try
            {
                ILCursor c = new ILCursor(il);

                c.GotoNext(i => i.MatchLdsfld(out FieldReference fref) && fref.Name == "gameMenu");
                c.EmitDelegate<Action>(BeforeOpenGameMenuAfterDisconnect);
            }
            catch (Exception e)
            {
                MonoModHooks.DumpIL(this, il);
            }
        }

        private void BeforeOpenGameMenuAfterDisconnect()
        {
            if (resetGameMenuAfterDisconnect)
            {
                // Player data is only saved when the game menu is closed before, this
                // was causing issues with lost data if you got an error when switching servers
                // where the game menu is reopened
                Main.gameMenu = false;
            }
            resetGameMenuAfterDisconnect = false;
        }

        private void HookGetData(ILContext il)
        {
            try
            {
                ILCursor c = new ILCursor(il);

                c.EmitDelegate<Action>(BeforeWorldInfoUpdate);
                c.GotoNext(i => i.MatchCall(out MethodReference mref) && mref.Name == "checkXMas");
                c.Index++;
                c.EmitDelegate<Action>(AfterWorldInfoUpdate);
            }
            catch (Exception e)
            {
                MonoModHooks.DumpIL(this, il);
            }
        }

        private void BeforeWorldInfoUpdate()
        {
            if (Main.netMode != 1)
                return;
            lastWorldUniqueId = Main.ActiveWorldFileData.UniqueId;
            lastWorldName = Main.worldName;
            lastWorldId = Main.worldID;
        }

        private void AfterWorldInfoUpdate()
        {
            if (Main.netMode != 1)
                return;
            var worldUniqueId = Main.ActiveWorldFileData.UniqueId;
            var worldName = Main.worldName;
            var worldId = Main.worldID;
            if (Netplay.Connection.State > 4 && lastWorldUniqueId != worldUniqueId)
            {
                Logger.Info("Updating mini map after switching worlds");
                // save previous map
                try
                {
                    Main.ActiveWorldFileData.UniqueId = lastWorldUniqueId;
                    Main.worldName = lastWorldName;
                    Main.worldID = lastWorldId;
                    Main.Map.Save();
                }
                finally
                {
                    Main.ActiveWorldFileData.UniqueId = worldUniqueId;
                    Main.worldName = worldName;
                    Main.worldID = worldId;
                }
                // restart from state 4, which clears the map and requests everything again
                Main.gameMenu = true;
                resetGameMenuAfterDisconnect = true;
                Main.menuMode = 14;
                Netplay.Connection.State = 4;
            }
        }
    }
}
