import { Browser } from 'webdriverio';

export default async function clickPermissionButton(
  driver: Browser,
  dialogText: string,
  buttonText: string,
): Promise<void> {
  const button = driver.$(
    `//*[@text="${dialogText}"]` +
      '/ancestor::*[@resource-id="com.android.permissioncontroller:id/content_container"]' +
      '/following-sibling::*' +
      `//*[@text="${buttonText}"]`,
  );

  await button.waitForDisplayed();
  await button.click();
}
