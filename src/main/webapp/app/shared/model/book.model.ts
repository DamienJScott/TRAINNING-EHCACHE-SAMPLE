export interface IBook {
  id?: number;
  name?: string;
  position?: string;
  author?: string;
  detail?: string;
}

export class Book implements IBook {
  constructor(public id?: number, public name?: string, public position?: string, public author?: string, public detail?: string) {}
}
