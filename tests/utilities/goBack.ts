import { Browser } from 'webdriverio';

export default async function goBack(browser: Browser): Promise<void> {
  const backButton = browser.$('accessibility id:Navigate up');
  await backButton.click();
}
