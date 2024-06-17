import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'sortEmoji',
  pure: false
})
export class SortEmojiPipe implements PipeTransform {
  transform(emojiDataValues: EmojiData[], args?: any): any {
    if (!Array.isArray(emojiDataValues) || emojiDataValues.length === 0) {
      return null;
    }
    return emojiDataValues.sort(
      (a: EmojiData, b: EmojiData) =>
        b.count - a.count || (a.emoji < b.emoji ? 1 : -1)
    );
  }
}