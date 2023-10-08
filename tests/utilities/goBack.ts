import { Browser } from 'webdriverio';

export default async function goBack(driver: Browser): Promise<void> {
  const backButton = driver.$('accessibility id:Navigate up');
  await backButton.click();
}
