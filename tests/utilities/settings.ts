import { Browser } from 'webdriverio';

export enum Setting {
  PrivateNumbers = 'Private numbers',
  UnknownNumbers = 'Numbers not in contacts',
}

export async function getSetting(
  browser: Browser,
  setting: Setting,
): Promise<boolean> {
  const privateNumbersSwitch = await browser.$(
    `//android.widget.TextView[@text="${setting}"]` +
      '/parent::android.widget.LinearLayout' +
      '/following-sibling::android.widget.Switch',
  );

  return (await privateNumbersSwitch.getAttribute('checked')) === 'true';
}

export async function openSettings(browser: Browser) {
  const optionsButton = await browser.$('accessibility id:More options');
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

export async function toggleSetting(
  browser: Browser,
  setting: Setting,
): Promise<void> {
  const privateNumbersText = await browser.$(
    `//android.widget.TextView[@text="${setting}"]`,
  );
  await privateNumbersText.waitForDisplayed();
  await privateNumbersText.click();
}
