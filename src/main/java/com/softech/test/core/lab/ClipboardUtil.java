package com.softech.test.core.lab;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;

import org.testng.Assert;

import com.softech.test.core.props.DesktopOSType;
import com.softech.test.core.util.Logger;

public class ClipboardUtil {
	
	public static String getBrowserClipboardContent() {
		String result = "";
		if (GridManager.isQALabHub()) {
			String activeBrowserIP = ActiveBrowserManager.getActiveBrowserAddress();
			if (ActiveBrowserManager.getActiveDesktopOSType().equals(DesktopOSType.MQE_MAC)) {
				result = CommandExecutor.execCommand("pbpaste", activeBrowserIP, null);
			} else {
				/* TODO - NOT YET SUPPORTED FOR WINDOWS
				CommandExecutor.execCommand("cat /dev/clipboard", activeBrowserIP, null);
				*/
				result = "clipboard capture on windows is not yet supported.";
			}
			return result;
		}
		
		// local clipboard
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        Transferable contents = clipboard.getContents(null);
        boolean hasTransferableText = (contents != null) &&
                contents.isDataFlavorSupported(DataFlavor.stringFlavor);
        if (hasTransferableText) {
            try {
                result = (String) contents.getTransferData(DataFlavor.stringFlavor);
            } catch (Exception ex) {
                ex.printStackTrace();
                Assert.fail("Failed to read from Clipboard.");
            }
        }
        return result;
	}
	
	public static void setBrowserClipboardContent(String clipboardTxt) {
		if (GridManager.isQALabHub()) {
			String activeBrowserIP = ActiveBrowserManager.getActiveBrowserAddress();
			if (ActiveBrowserManager.getActiveDesktopOSType().equals(DesktopOSType.MQE_MAC)) {
				CommandExecutor.execCommand("echo '" + clipboardTxt + "' | pbcopy", activeBrowserIP, null);
			} else {
				Logger.logConsoleMessage("clipboard on windows is not yet supported.");
			}
		} else {
			// local clipboard
			StringSelection stringSelection = new StringSelection(clipboardTxt);
			Toolkit.getDefaultToolkit().getSystemClipboard().setContents(stringSelection, null);
		}
	}
	
}
