import { Button, Card, CardAction, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from 'homie-ui';

export const Default = () => (
  <Card style={{ width: 340 }}>
    <CardHeader>
      <CardTitle>Weekly meal plan</CardTitle>
      <CardDescription>Five dinners, planned for the whole family.</CardDescription>
    </CardHeader>
    <CardContent>
      <p>Monday: Spaghetti Carbonara · Tuesday: Lentil Soup · Wednesday: Chicken Curry</p>
    </CardContent>
    <CardFooter style={{ gap: 8 }}>
      <Button size="sm">Open plan</Button>
      <Button size="sm" variant="outline">Share</Button>
    </CardFooter>
  </Card>
);

export const WithAction = () => (
  <Card style={{ width: 340 }}>
    <CardHeader>
      <CardTitle>Shopping list</CardTitle>
      <CardDescription>12 items for this week</CardDescription>
      <CardAction>
        <Button size="sm" variant="ghost">Clear</Button>
      </CardAction>
    </CardHeader>
    <CardContent>
      <p>Spaghetti, guanciale, eggs, pecorino, red lentils, coconut milk…</p>
    </CardContent>
  </Card>
);
