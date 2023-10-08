import { Browser } from 'webdriverio';

export async function waitForEmptyLog(driver: Browser): Promise<void> {
  const emptyView = await driver.$('id:com.novyr.callfilter:id/empty_view');
  await emptyView.waitForDisplayed();
}

export async function waitForLogEntry(
  driver: Browser,
  message: string,
): Promise<void> {
  const log = await driver.$(
    `//*[@resource-id="com.novyr.callfilter:id/log_list_message"][@text="${message}"]`,
  );
  await log.waitForDisplayed();
}
