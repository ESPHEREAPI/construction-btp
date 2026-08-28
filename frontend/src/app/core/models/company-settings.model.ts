export interface CompanySettings {
  currency: string | null;
}

/** Common presets shown in the settings dropdown - the company can also type a custom label if theirs isn't listed. */
export const CURRENCY_PRESETS: { label: string; value: string }[] = [
  { label: 'FCFA (Afrique Centrale/Ouest)', value: 'FCFA' },
  { label: 'Naira (Nigeria)', value: 'Naira' },
  { label: 'Cedi (Ghana)', value: 'Cedi' },
  { label: 'Dollar (USD)', value: 'Dollar' },
  { label: 'Euro (EUR)', value: 'Euro' }
];

export const DEFAULT_CURRENCY = 'FCFA';
