import { Browser, remote, RemoteOptions } from 'webdriverio';
import { Capabilities } from '@wdio/types';
import { it } from 'vitest';
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

export default async function itWithDriver(
  description: string,
  callback: (browser: Browser) => Promise<void>,
): Promise<void> {
  it(description, async () => {
    const driver = await remote(wdOpts);

    try {
      await callback(driver);
    } finally {
      await driver.deleteSession();
    }
  });
}
