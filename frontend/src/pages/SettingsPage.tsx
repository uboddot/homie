import { type FormEvent, useEffect, useState } from 'react';
import { toast } from 'sonner';
import { Button } from '@/Components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/Components/ui/card';
import { Label } from '@/Components/ui/label';
import { Switch } from '@/Components/ui/switch';
import { apiFetch } from '../api/client';
import LoadingBorder from '../Components/LoadingBorder';
import MealTargetField from '../Components/Settings/MealTargetField';
import { type MealTargetRow, mealTargetError, toMealTarget, toMealTargetRow } from '../Components/Settings/mealTarget';
import type { Settings } from '../model/Settings';
import './SettingsPage.css';

export default function SettingsPage() {
  const [loaded, setLoaded] = useState(false);
  const [loadError, setLoadError] = useState<string | null>(null);
  const [useAi, setUseAi] = useState(true);
  const [protein, setProtein] = useState<MealTargetRow>(() => toMealTargetRow(null));
  const [calories, setCalories] = useState<MealTargetRow>(() => toMealTargetRow(null));
  const [error, setError] = useState<string | null>(null);
  const [saving, setSaving] = useState(false);

  function apply(settings: Settings) {
    setUseAi(settings.useAi);
    setProtein(toMealTargetRow(settings.protein));
    setCalories(toMealTargetRow(settings.calories));
  }

  useEffect(() => {
    apiFetch<Settings>('/settings')
      .then(settings => {
        apply(settings);
        setLoaded(true);
      })
      .catch(err => setLoadError(err.message));
  }, []);

  async function handleSubmit(event: FormEvent) {
    event.preventDefault();
    const invalid = mealTargetError(protein) ?? mealTargetError(calories);
    if (invalid) {
      setError(invalid);
      return;
    }

    const body: Settings = { useAi, protein: toMealTarget(protein), calories: toMealTarget(calories) };
    setError(null);
    setSaving(true);
    try {
      apply(await apiFetch<Settings>('/settings', { method: 'PUT', body: JSON.stringify(body) }));
      toast.success('Settings saved');
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Couldn’t save the settings');
    } finally {
      setSaving(false);
    }
  }

  if (loadError) {
    return <div className="page-message">{loadError}</div>;
  }

  if (!loaded) {
    return <div className="page-message">Loading...</div>;
  }

  return (
    <div className="page settings-page">
      <h1 className="settings-title">Settings</h1>

      <form className="settings-form" onSubmit={handleSubmit} noValidate>
        <Card>
          <CardHeader>
            <CardTitle>AI features</CardTitle>
            <CardDescription>
              Calculates nutrition info for new ingredients with Google Gemini. When off, only ingredients Homie
              already knows are used.
            </CardDescription>
          </CardHeader>
          <CardContent>
            <div className="settings-switch">
              <Switch id="settings-use-ai" checked={useAi} onCheckedChange={setUseAi} />
              <Label htmlFor="settings-use-ai">Use AI features</Label>
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Goals per meal</CardTitle>
            <CardDescription>How much protein and energy you'd like a meal to have.</CardDescription>
          </CardHeader>
          <CardContent className="settings-targets">
            <MealTargetField id="settings-protein" label="Protein" unit="g" value={protein} onChange={setProtein} />
            <MealTargetField id="settings-calories" label="Calories" unit="kcal" value={calories}
              onChange={setCalories} />
          </CardContent>
        </Card>

        {error && <p className="settings-error" role="alert">{error}</p>}

        <div className="settings-actions">
          <LoadingBorder active={saving}>
            <Button type="submit" disabled={saving}>{saving ? 'Saving...' : 'Save settings'}</Button>
          </LoadingBorder>
        </div>
      </form>
    </div>
  );
}
