import { Browser } from 'webdriverio';

export default async function hasSpamAppSelection(
  driver: Browser,
  dialogText: string,
): Promise<boolean> {
  const radioButton = await driver.$(
    `//*[@text="${dialogText}"]` +
      '/parent::android.widget.LinearLayout' +
      '/parent::android.widget.LinearLayout' +
      '/following-sibling::android.widget.FrameLayout' +
      `//android.widget.TextView[@text="Call Filter"]` +
      '/parent::android.widget.LinearLayout' +
      '/following-sibling::android.widget.RadioButton',
  );

  return radioButton.isDisplayed();
}
