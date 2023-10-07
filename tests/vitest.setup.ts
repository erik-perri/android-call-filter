import { afterAll, beforeEach } from 'vitest';
import { closeDriver } from './utilities/driver';

beforeEach(async (context) => {
  await closeDriver();
});

afterAll(async () => {
  await closeDriver();
});
