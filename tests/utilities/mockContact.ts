import { Browser } from 'webdriverio';

async function hasMockContact(
  driver: Browser,
  phoneNumber: string,
): Promise<boolean> {
  const result = await driver.executeScript('mobile: shell', [
    {
      command: 'content',
      args: `query --uri content://com.android.contacts/data/phones/filter/${phoneNumber} --projection contact_id`,
    },
  ]);

  return typeof result === 'string' && result.trim().includes('contact_id=');
}

export default async function createMockContact(
  browser: Browser,
  name: string,
  phoneNumber: string,
): Promise<void> {
  if (await hasMockContact(browser, phoneNumber)) {
    return;
  }

  const originalPackage = await browser.getCurrentPackage();
  const originalActivity = await browser.getCurrentActivity();

  await browser.executeScript('mobile: shell', [
    {
      command: 'am',
      args:
        'start ' +
        '-a android.intent.action.INSERT ' +
        '-t vnd.android.cursor.dir/contact ' +
        `-e name "${name}" ` +
        `-e phone "${phoneNumber}"`,
    },
  ]);

  const saveButton = await browser.$(
    'id:com.android.contacts:id/editor_menu_save_button',
  );
  await saveButton.click();
  await browser.pause(500);

  await browser.startActivity(originalPackage, originalActivity);
}
