import { describe, expect, it } from 'vitest';
import allowPermissions from '../utilities/allowPermissions';
import { initializeDriver } from '../utilities/driver';
import goBack from '../utilities/goBack';
import {
  getSetting,
  openSettings,
  RuleAction,
  RuleEnabled,
  RuleMatchType,
  toggleSetting,
} from '../utilities/settings';
import { waitForEmptyLog, waitForLogEntry } from '../utilities/log';
import createMockContact from '../utilities/mockContact';
import addNewRule from '../utilities/addNewRule';

describe('FilterRules', () => {
  it.each([
    [
      RuleMatchType.PrivateNumbers,
      {
        log: 'Blocked call: Private',
        number: '#',
      },
    ],
    [
      RuleMatchType.NumbersNotInContacts,
      {
        log: 'Blocked call: (555) 123-4657',
        number: '5551234657',
      },
    ],
  ])('should block %s if set', async (setting, { log, number }) => {
    const driver = await initializeDriver();
    await allowPermissions(driver);
    await waitForEmptyLog(driver);

    await openSettings(driver);
    await toggleSetting(driver, setting);
    expect(await getSetting(driver, setting)).toBe(true);
    await goBack(driver);
    await driver.gsmCall(number, 'call');
    await waitForLogEntry(driver, log);
  });

  it.each([
    [
      RuleMatchType.PrivateNumbers,
      {
        log: 'Allowed call: Private',
        number: '#',
      },
    ],
    [
      RuleMatchType.NumbersNotInContacts,
      {
        log: 'Allowed call: (555) 123-4657',
        number: '5551234657',
      },
    ],
  ])('should allow %s if not set', async (setting, { log, number }) => {
    const driver = await initializeDriver();
    await allowPermissions(driver);
    await waitForEmptyLog(driver);

    await openSettings(driver);
    expect(await getSetting(driver, setting)).toBe(false);
    await goBack(driver);
    await driver.gsmCall(number, 'call');
    await driver.pause(1000);
    await driver.gsmCall(number, 'cancel');
    await driver.pause(1000);
    await waitForLogEntry(driver, log);
  });

  it('should allow calls by contacts if set', async () => {
    const driver = await initializeDriver();
    await allowPermissions(driver);
    await waitForEmptyLog(driver);

    const mockContactNumber = '5551234';
    const mockContactName = 'Mock User';
    await createMockContact(driver, mockContactName, mockContactNumber);

    await openSettings(driver);
    await toggleSetting(driver, RuleMatchType.NumbersNotInContacts);
    expect(await getSetting(driver, RuleMatchType.NumbersNotInContacts)).toBe(
      true,
    );
    await goBack(driver);
    await driver.gsmCall(mockContactNumber, 'call');
    await driver.pause(1000);
    await driver.gsmCall(mockContactNumber, 'cancel');
    await driver.pause(1000);
    await waitForLogEntry(driver, `Allowed call: ${mockContactName}`);
  });

  it('should block calls by contacts if set', async () => {
    const driver = await initializeDriver();
    await allowPermissions(driver);
    await waitForEmptyLog(driver);

    const mockContactNumber = '5551234';
    const mockContactName = 'Mock User';
    await createMockContact(driver, mockContactName, mockContactNumber);

    await openSettings(driver);
    await addNewRule(
      driver,
      RuleEnabled.Yes,
      RuleAction.Block,
      RuleMatchType.NumbersInContacts,
    );

    expect(await getSetting(driver, RuleMatchType.NumbersInContacts)).toBe(
      true,
    );
    await goBack(driver);
    await driver.gsmCall(mockContactNumber, 'call');
    await waitForLogEntry(driver, `Blocked call: ${mockContactName}`);
  });
});
