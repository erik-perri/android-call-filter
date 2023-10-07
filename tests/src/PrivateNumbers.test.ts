import { describe, expect, it } from 'vitest';
import allowPermissions from '../utilities/allowPermissions';
import { Browser } from 'webdriverio';
import { initializeDriver } from '../utilities/driver';

describe('PrivateNumbers', () => {
  it('should block private numbers if specified', async () => {
    const browser = await initializeDriver();
    await allowPermissions(browser);

    const emptyView = await browser.$('id:com.novyr.callfilter:id/empty_view');
    await emptyView.waitForDisplayed();

    await openSettings(browser);
    expect(await getPrivateNumberSetting(browser)).toBe(false);
    await goBack(browser);
    await browser.gsmCall('#', 'call');
    await browser.pause(500);
    await browser.gsmCall('#', 'cancel');
    await browser.pause(500);
    await waitForLog(browser, 'Allowed call: Private');

    await openSettings(browser);
    await togglePrivateNumbersSetting(browser);
    expect(await getPrivateNumberSetting(browser)).toBe(true);
    await goBack(browser);
    await browser.gsmCall('#', 'call');
    await browser.pause(500);
    await waitForLog(browser, 'Blocked call: Private');
  });
});

async function openSettings(browser: Browser) {
  const optionsButton = await browser.$('//*[@content-desc="More options"]');
  await optionsButton.waitForDisplayed();
  await optionsButton.click();

  const settingsButton = await browser.$(
    '//android.widget.TextView[@text="Settings"]',
  );
  await settingsButton.waitForDisplayed();
  await settingsButton.click();

  const filterRulesHeading = await browser.$(
    '//android.widget.TextView[@text="Filter Rules"]',
  );
  await filterRulesHeading.waitForDisplayed();
}

async function togglePrivateNumbersSetting(browser: Browser): Promise<void> {
  const privateNumbersText = await browser.$(
    '//android.widget.TextView[@text="Private numbers"]',
  );
  await privateNumbersText.waitForDisplayed();
  await privateNumbersText.click();
}

async function getPrivateNumberSetting(browser: Browser): Promise<boolean> {
  const privateNumbersSwitch = await browser.$(
    '//android.widget.TextView[@text="Private numbers"]' +
      '/parent::android.widget.LinearLayout' +
      '/following-sibling::android.widget.Switch',
  );

  return (await privateNumbersSwitch.getAttribute('checked')) === 'true';
}

async function goBack(browser: Browser): Promise<void> {
  const backButton = browser.$('accessibility id:Navigate up');
  await backButton.click();
}

async function waitForLog(browser: Browser, message: string): Promise<void> {
  const log = await browser.$(`//android.widget.TextView[@text="${message}"]`);
  await log.waitForDisplayed();
}
