import { afterAll, beforeAll, beforeEach } from 'vitest';
import { closeDriver, getCurrentDriver } from './utilities/driver';
import { clearDebugFiles, writeDebugFiles } from './utilities/debug';

beforeAll(async () => {
  await clearDebugFiles();
});

beforeEach(async (context) => {
  await closeDriver();

  context.onTestFailed(async () => {
    const driver = getCurrentDriver();
    if (driver) {
      await writeDebugFiles(context.task, driver);
    }
  });
});

afterAll(async () => {
  await closeDriver();
});
