import { Button, Sheet, SheetContent, SheetDescription, SheetFooter, SheetHeader, SheetTitle } from 'homie-ui';

export const OpenRight = () => (
  <Sheet open>
    <SheetContent side="right">
      <SheetHeader>
        <SheetTitle>Menu</SheetTitle>
        <SheetDescription>Signed in as anna</SheetDescription>
      </SheetHeader>
      <nav style={{ display: 'grid', gap: 4, padding: '0 16px' }}>
        <Button variant="ghost" style={{ justifyContent: 'flex-start' }}>Home</Button>
        <Button variant="ghost" style={{ justifyContent: 'flex-start' }}>Recipes</Button>
      </nav>
      <SheetFooter>
        <Button variant="outline">Log out</Button>
      </SheetFooter>
    </SheetContent>
  </Sheet>
);
