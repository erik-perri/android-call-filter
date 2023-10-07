import * as fs from 'fs';
import path from 'path';
import { Browser } from 'webdriverio';
import { Test } from 'vitest';

export function getDebugPath(type: 'screenshots' | 'sources'): string {
  return path.resolve(__dirname, `../failures/${type}`);
}

function getExistingDebugFiles(type: 'screenshots' | 'sources'): string[] {
  return fs
    .readdirSync(`${getDebugPath(type)}`)
    .filter((file) => file[0] !== '.')
    .map((file) => `${getDebugPath(type)}/${file}`);
}

export async function clearDebugFiles(): Promise<void> {
  [
    getExistingDebugFiles('screenshots'),
    getExistingDebugFiles('sources'),
  ].forEach((files) => files.forEach((file) => fs.unlinkSync(file)));
}

export async function writeDebugFiles(
  test: Test,
  driver: Browser,
): Promise<void> {
  const name = getTestFileName(test);

  await driver.saveScreenshot(
    path.resolve(__dirname, `${getDebugPath('screenshots')}/${name}.png`),
  );

  const source = await driver.getPageSource();
  fs.writeFileSync(
    path.resolve(__dirname, `${getDebugPath('sources')}/${name}.xml`),
    source,
  );
}

const knownTests = new Map<string, number>();

function getTestFileName(test: Test): string {
  const taskName = test.name.replace(/[^a-z0-9]/gi, '_').toLowerCase();
  const suiteName = test.suite.name.replace(/[^a-z0-9]/gi, '_');

  knownTests.set(
    `${suiteName}_${taskName}`,
    knownTests.has(`${suiteName}_${taskName}`)
      ? (knownTests.get(`${suiteName}_${taskName}`) ?? 0) + 1
      : 0,
  );

  return `${suiteName}_${taskName}_${knownTests.get(
    `${suiteName}_${taskName}`,
  )}`;
}
