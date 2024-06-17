import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'sliceCollection',
  pure: false
})
export class SliceCollectionPipe implements PipeTransform {
  transform(emojiDataValues: EmojiData[], length: number, args?: any): any {
    if (!Array.isArray(emojiDataValues) || emojiDataValues.length === 0) {
      return null;
    }
    return emojiDataValues.slice(0, length);
  }
}
