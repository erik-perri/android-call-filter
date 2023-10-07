import { Browser } from 'webdriverio';

export default async function clickSpamAppSelection(
  driver: Browser,
  dialogText: string,
  appName: string,
  buttonText: string,
): Promise<void> {
  const radioButton = await driver.$(
    `//*[@text="${dialogText}"]` +
      '/ancestor::*[@resource-id="android:id/parentPanel"]' +
      `//*[@text="${appName}"]` +
      '/parent::*' +
      '/following-sibling::android.widget.RadioButton',
  );

  await radioButton.waitForDisplayed();
  await radioButton.click();

  const finishButton = await driver.$(
    `//*[@text="${dialogText}"]` +
      '/ancestor::*[@resource-id="android:id/parentPanel"]' +
      `//*[@text="${buttonText}"]`,
  );

  await finishButton.waitForDisplayed();
  await finishButton.click();
}
