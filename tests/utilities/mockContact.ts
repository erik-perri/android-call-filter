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
  driver: Browser,
  name: string,
  phoneNumber: string,
): Promise<void> {
  if (await hasMockContact(driver, phoneNumber)) {
    return;
  }

  const originalPackage = await driver.getCurrentPackage();
  const originalActivity = await driver.getCurrentActivity();

  await driver.executeScript('mobile: shell', [
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

  await driver.pause(250);

  const accountModalCancelButton = await driver.$(
    '//*[@text="Take a minute to add an account that will back up your contacts to Google."]' +
      '/ancestor::*[@resource-id="android:id/content"]' +
      '//android.widget.Button[@text="CANCEL"]',
  );
  if (await accountModalCancelButton.isDisplayed()) {
    await accountModalCancelButton.click();
  }

  const saveButton = await driver.$(
    'id:com.android.contacts:id/editor_menu_save_button',
  );
  await saveButton.click();
  await driver.pause(500);

  await driver.startActivity(originalPackage, originalActivity);
}
