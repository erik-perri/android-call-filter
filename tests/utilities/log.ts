import { Browser } from 'webdriverio';

export async function waitForEmptyLog(browser: Browser): Promise<void> {
  const emptyView = await browser.$('id:com.novyr.callfilter:id/empty_view');
  await emptyView.waitForDisplayed();
}

export async function waitForLogEntry(
  browser: Browser,
  message: string,
): Promise<void> {
  const log = await browser.$(
    `//*[@resource-id="com.novyr.callfilter:id/log_list_message"][@text="${message}"]`,
  );
  await log.waitForDisplayed();
}
