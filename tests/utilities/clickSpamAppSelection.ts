import { Browser } from 'webdriverio';

export default async function clickSpamAppSelection(
  driver: Browser,
  dialogText: string,
  appName: string,
  buttonText: string,
): Promise<void> {
  const radioButton = await driver.$(
    `//*[@text="${dialogText}"]` +
      '/parent::android.widget.LinearLayout' +
      '/parent::android.widget.LinearLayout' +
      '/following-sibling::android.widget.FrameLayout' +
      `//android.widget.TextView[@text="${appName}"]` +
      '/parent::android.widget.LinearLayout' +
      '/following-sibling::android.widget.RadioButton',
  );

  await radioButton.waitForDisplayed();
  await radioButton.click();

  const finishButton = await driver.$(
    `//*[@text="${dialogText}"]` +
      '/parent::android.widget.LinearLayout' +
      '/parent::android.widget.LinearLayout' +
      '/following-sibling::android.widget.FrameLayout' +
      '/following-sibling::android.widget.ScrollView' +
      `//android.widget.Button[@text="${buttonText}"]`,
  );

  await finishButton.waitForDisplayed();
  await finishButton.click();
}
