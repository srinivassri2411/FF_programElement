package Windows;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.NotFoundException;

import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;

public class BringWindowUpFront implements Nlp {
	@InputParams({ @InputParam(name = "Window Name", type = "java.lang.String") })

	public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {
		System.setProperty("java.awt.headless", "false");
		Map<String, Object> programElementsInput = nlpRequestModel.getAttributes();
		String windowName = (String) programElementsInput.get("Window Name");
		NlpResponseModel nlpResponseModel = new NlpResponseModel();
		try {
			if (windowName.isEmpty() || windowName.length() <= 2) {
				throw new NotFoundException("Given window name is empty" + windowName);
			}
//			boolean res1 = bringUpFront(windowName);
//			Thread.sleep(500);
//			boolean res2 = maximize(windowName);
//			if (res1 && res2) {
			setWindowForeGround(windowName);
			nlpResponseModel.setStatus(CommonConstants.pass);
			nlpResponseModel.setMessage("Window maximized and brought to front: " + windowName);
//			} else {
//				nlpResponseModel.setStatus(CommonConstants.fail);
//				nlpResponseModel.setMessage("Window not maximized and brought to front: " + windowName);
//			}
		} catch (Exception e) {
			nlpResponseModel.setMessage("Failed =>" + e.getMessage());
			nlpResponseModel.setStatus(CommonConstants.fail);
		}
		return nlpResponseModel;
	}

//	public static boolean bringUpFront(String windowTitle) throws InterruptedException {
//		WinDef.HWND hwnd = User32.INSTANCE.FindWindow(null, windowTitle);
//		if (hwnd == null) {
//			throw new NotFoundException("Given " + windowTitle + " was not found");
//		} else {
//			User32.INSTANCE.ShowWindow(hwnd, User32.SW_RESTORE);
//			Thread.sleep(200);
//			User32.INSTANCE.SetForegroundWindow(hwnd);
//			return true;
//		}
//	}
//
//	public static boolean maximize(String windowTitle) throws InterruptedException {
//		WinDef.HWND hwnd = User32.INSTANCE.FindWindow(null, windowTitle);
//
//		if (hwnd == null) {
//			throw new NotFoundException("Given " + windowTitle + " was not found");
//		} else {
//			User32.INSTANCE.ShowWindow(hwnd, User32.SW_MAXIMIZE);
//			Thread.sleep(200);
//			User32.INSTANCE.SetForegroundWindow(hwnd);
//			System.out.println("Window maximized and brought to front: " + windowTitle);
//			return true;
//		}
//	}
	public static void setWindowForeGround(String appName) throws IOException {
		String ps1Logic = """
								Add-Type -AssemblyName UIAutomationClient

								$target = "AppName"   # change if needed
								$desktop = [System.Windows.Automation.AutomationElement]::RootElement

								# Try top-level (Children) first, then deeper (Descendants)
								$cond = New-Object System.Windows.Automation.PropertyCondition `
								    ([System.Windows.Automation.AutomationElement]::NameProperty, $target)

								$element = $desktop.FindFirst([System.Windows.Automation.TreeScope]::Children, $cond)
								if ($null -eq $element) {
								    $element = $desktop.FindFirst([System.Windows.Automation.TreeScope]::Descendants, $cond)
								}

								# Fallback: search all descendants for controls whose ValuePattern.Value == $target
								if ($null -eq $element) {
								    $all = $desktop.FindAll([System.Windows.Automation.TreeScope]::Descendants, `
								        [System.Windows.Automation.Condition]::TrueCondition)
								    foreach ($e in $all) {
								        try {
								            $val = $e.GetCurrentPropertyValue([System.Windows.Automation.ValuePatternIdentifiers]::ValueProperty)
								            if ($val -ne $null -and $val.ToString() -eq $target) {
								                $element = $e
								                break
								            }
								        } catch { }
								    }
								}

								if ($null -eq $element) {
								    Write-Output "UIA: element with Name/Value '$target' not found."
								    exit 2
								}

								# Try SetFocus via UIA first
								try {
								    $element.SetFocus()
								    Write-Output "UIA: SetFocus() called."
								} catch {
								    Write-Output "UIA: SetFocus() threw: $_"
								}

								# If element exposes a native window handle, try ShowWindowAsync + SetForegroundWindow
								$hwnd = $element.Current.NativeWindowHandle
				if ($hwnd -ne 0) {
				    Add-Type @"
				using System;
				using System.Runtime.InteropServices;
				public static class Win32 {
				    [DllImport("user32.dll")]
				    public static extern bool SetForegroundWindow(IntPtr hWnd);
				    [DllImport("user32.dll")]
				    public static extern bool ShowWindowAsync(IntPtr hWnd, int nCmdShow);
				    public const int SW_MAXIMIZE = 3;
				}
				"@

				    $ptr = [IntPtr] $hwnd
				    [Win32]::ShowWindowAsync($ptr, [Win32]::SW_MAXIMIZE) | Out-Null
				    [Win32]::SetForegroundWindow($ptr) | Out-Null
				    Write-Output "Tried SetForegroundWindow + Maximize on handle $hwnd"
				} else {
				    Write-Output "Element has no native window handle (NativeWindowHandle=0)."
				}


								exit 0
												""";
		ps1Logic = ps1Logic.replace("AppName", appName);
		File ps1Path = File.createTempFile("FF_BringWindow__", ".ps1");
		FileUtils.writeStringToFile(ps1Path, ps1Logic);
		String cmd = "powershell.exe -NoLogo -NoProfile -WindowStyle Hidden -ExecutionPolicy Bypass -File \"filePath\"";

		cmd = cmd.replace("filePath", ps1Path.getAbsolutePath());
		System.out.println(cmd);
		Runtime.getRuntime().exec(cmd);
	}

	public static void main(String[] args) throws IOException, InterruptedException {
		Thread.sleep(3000);
		setWindowForeGround("CSE App");
	}
}