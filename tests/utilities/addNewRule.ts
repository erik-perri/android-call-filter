import { Browser } from 'webdriverio';
import { RuleAction, RuleEnabled, RuleMatchType } from './settings';

export default async function addNewRule(
  driver: Browser,
  status: RuleEnabled,
  action: RuleAction,
  matchType: RuleMatchType,
) {
  const addRuleButton = driver.$('accessibility id:Add new rule');
  await addRuleButton.click();

  const enabledSpinner = driver.$('id:com.novyr.callfilter:id/enabled_spinner');
  await enabledSpinner.click();

  const enabledOption = driver.$(
    `//android.widget.CheckedTextView[@text="${status}"]`,
  );
  await enabledOption.click();

  const actionSpinner = driver.$('id:com.novyr.callfilter:id/action_spinner');
  await actionSpinner.click();

  const actionOption = driver.$(
    `//android.widget.CheckedTextView[@text="${action}"]`,
  );
  await actionOption.click();

  const typeSpinner = driver.$('id:com.novyr.callfilter:id/type_spinner');
  await typeSpinner.click();

  const typeOption = driver.$(
    `//android.widget.CheckedTextView[@text="${matchType}"]`,
  );
  await typeOption.click();

  const okButton = driver.$('id:android:id/button1');
  await okButton.click();
}
