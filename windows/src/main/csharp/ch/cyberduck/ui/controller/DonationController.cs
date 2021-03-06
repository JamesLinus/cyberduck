﻿// 
// Copyright (c) 2010-2014 Yves Langisch. All rights reserved.
// http://cyberduck.ch/
// 
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
// 
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
// 
// Bug fixes, suggestions and comments should be sent to:
// yves@cyberduck.ch
// 

using System;
using System.Reflection;
using System.Windows.Forms;
using ch.cyberduck.core;
using ch.cyberduck.core.local;
using ch.cyberduck.core.notification;
using ch.cyberduck.core.preferences;
using Ch.Cyberduck.Core.TaskDialog;
using Ch.Cyberduck.Ui.Growl;
using StructureMap;

namespace Ch.Cyberduck.Ui.Controller
{
    public class DonationController : IDonationController
    {
        private static string Localize(string text) => LocaleFactory.localizedString(text, "Donate");

        public DonationController()
        {
        }

        public void Show()
        {
            int uses = PreferencesFactory.get().getInteger("uses");

            var notify = (ToolstripNotificationService)NotificationServiceFactory.get();

            var result = TaskDialog.Show(
                owner: IntPtr.Zero,
                allowDialogCancellation: true,
                title: Localize("Please Donate") + " (" + uses + ")",
                verificationText: Localize("Don't show again for this version."),
                mainInstruction: Localize("Thank you for using Cyberduck!"),
                content: $@"{Localize("It has taken many nights to develop this application. If you enjoy using it, please consider a donation to the author of this software. It will help to make Cyberduck even better!")}

{Localize("The payment can be made simply and safely using Paypal. You don't need to open an account.")}",
                expandedInfo: $@"{Localize("Donation Key")}

{Localize("As a contributor to Cyberduck, you receive a donation key that disables this prompt.")}",
                commandLinks: new[] { Localize("Donate!"), Localize("Later") },
                expandedByDefault: true,
                verificationByDefault: Assembly.GetExecutingAssembly().GetName().Version.ToString().Equals(PreferencesFactory.get().getProperty("donate.reminder")));
            if (result.VerificationChecked == true)
            {
                PreferencesFactory.get().setProperty("donate.reminder", Assembly.GetExecutingAssembly().GetName().Version.ToString());
            }

            if (result.CommandButtonResult == 0)
            {
                BrowserLauncherFactory.get().open(PreferencesFactory.get().getProperty("website.donate"));
            }

            PreferencesFactory.get().setProperty("donate.reminder.date", DateTime.Now.Ticks);
        }
    }
}
