import { Browser } from 'webdriverio';

export default async function clickPermissionButton(
  driver: Browser,
  dialogText: string,
  buttonText: string,
): Promise<void> {
  const button = driver.$(
    `//*[@text="${dialogText}"]` +
      '/parent::android.widget.LinearLayout' +
      '/parent::android.widget.LinearLayout' +
      '/following-sibling::android.widget.LinearLayout' +
      `/android.widget.Button[@text="${buttonText}"]`,
  );

  await button.waitForDisplayed();
  await button.click();
}
