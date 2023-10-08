import { Browser } from 'webdriverio';
import { RuleAction, RuleEnabled, RuleMatchType } from './settings';

export default async function addNewRule(
  browser: Browser,
  status: RuleEnabled,
  action: RuleAction,
  matchType: RuleMatchType,
) {
  const addRuleButton = browser.$('accessibility id:Add new rule');
  await addRuleButton.click();

  const enabledSpinner = browser.$(
    'id:com.novyr.callfilter:id/enabled_spinner',
  );
  await enabledSpinner.click();

  const enabledOption = browser.$(
    `//android.widget.CheckedTextView[@text="${status}"]`,
  );
  await enabledOption.click();

  const actionSpinner = browser.$('id:com.novyr.callfilter:id/action_spinner');
  await actionSpinner.click();

  const actionOption = browser.$(
    `//android.widget.CheckedTextView[@text="${action}"]`,
  );
  await actionOption.click();

  const typeSpinner = browser.$('id:com.novyr.callfilter:id/type_spinner');
  await typeSpinner.click();

  const typeOption = browser.$(
    `//android.widget.CheckedTextView[@text="${matchType}"]`,
  );
  await typeOption.click();

  const okButton = browser.$('id:android:id/button1');
  await okButton.click();
}
