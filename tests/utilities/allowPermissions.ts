import { Browser } from 'webdriverio';
import clickPermissionButton from './clickPermissionButton';
import hasSpamAppSelection from './hasSpamAppSelection';
import clickSpamAppSelection from './clickSpamAppSelection';

export default async function allowPermissions(driver: Browser): Promise<void> {
  await clickPermissionButton(
    driver,
    'Allow Call Filter to access your contacts?',
    'ALLOW',
  );

  const spamDialogText =
    'Set Call Filter as your default caller ID & spam app?';
  if (await hasSpamAppSelection(driver, spamDialogText)) {
    await clickSpamAppSelection(
      driver,
      spamDialogText,
      'Call Filter',
      'SET AS DEFAULT',
    );
  }
}
