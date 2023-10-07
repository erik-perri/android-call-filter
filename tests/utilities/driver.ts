import { Browser, remote, RemoteOptions } from 'webdriverio';
import { Capabilities } from '@wdio/types';
import path from 'path';
import getEnv from './getEnv';

const capabilities: Capabilities.RemoteCapability = {
  'appium:app': path.resolve(
    __dirname,
    '../../app/build/outputs/apk/debug/app-debug.apk',
  ),
  'appium:appActivity': '.ui.loglist.LogListActivity',
  'appium:appPackage': 'com.novyr.callfilter',
  'appium:automationName': 'UiAutomator2',
  'appium:deviceName': 'Android',
  platformName: 'Android',
};

const wdOpts: RemoteOptions = {
  hostname: getEnv('APPIUM_HOST', '127.0.0.1'),
  port: parseInt(getEnv('APPIUM_PORT', '4723'), 10),
  logLevel: 'warn',
  capabilities,
};

let currentDriver: Browser | undefined;

export function getCurrentDriver(): Browser | undefined {
  return currentDriver;
}

export async function initializeDriver(): Promise<Browser> {
  currentDriver ??= await remote(wdOpts);
  return currentDriver;
}

export async function closeDriver(): Promise<void> {
  if (currentDriver) {
    await currentDriver.deleteSession();
    currentDriver = undefined;
  }
}
