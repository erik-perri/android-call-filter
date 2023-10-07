import { describe, expect, it } from 'vitest';
import allowPermissions from '../utilities/allowPermissions';
import { initializeDriver } from '../utilities/driver';
import goBack from '../utilities/goBack';
import {
  getSetting,
  openSettings,
  Setting,
  toggleSetting,
} from '../utilities/settings';
import { waitForEmptyLog, waitForLogEntry } from '../utilities/log';

describe('FilterRules', () => {
  it.each([
    [
      Setting.PrivateNumbers,
      {
        log: 'Blocked call: Private',
        number: '#',
      },
    ],
    [
      Setting.UnknownNumbers,
      {
        log: 'Blocked call: (555) 123-4657',
        number: '5551234657',
      },
    ],
  ])('should block %s if set', async (setting, { log, number }) => {
    const browser = await initializeDriver();
    await allowPermissions(browser);
    await waitForEmptyLog(browser);

    await openSettings(browser);
    await toggleSetting(browser, setting);
    expect(await getSetting(browser, setting)).toBe(true);
    await goBack(browser);
    await browser.gsmCall(number, 'call');
    await waitForLogEntry(browser, log);
  });

  it.each([
    [
      Setting.PrivateNumbers,
      {
        log: 'Allowed call: Private',
        number: '#',
      },
    ],
    [
      Setting.UnknownNumbers,
      {
        log: 'Allowed call: (555) 123-4657',
        number: '5551234657',
      },
    ],
  ])('should allow %s if not set', async (setting, { log, number }) => {
    const browser = await initializeDriver();
    await allowPermissions(browser);
    await waitForEmptyLog(browser);

    await openSettings(browser);
    expect(await getSetting(browser, setting)).toBe(false);
    await goBack(browser);
    await browser.gsmCall(number, 'call');
    await browser.pause(1000);
    await browser.gsmCall(number, 'cancel');
    await browser.pause(1000);
    await waitForLogEntry(browser, log);
  });
});
