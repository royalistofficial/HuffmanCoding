# Документация для сохранения закодированных данных и кодов Хаффмана

## Описание

Данный проект представляет собой программу для сжатия и распаковки файлов. Она принимает входной файл, обрабатывает его и сохраняет результат в выходной файл. Программа поддерживает как сжатие, так и декодирование (распаковку) данных.

## Использование

Программа принимает следующие аргументы командной строки:

- `-i` или `--input` - указывает имя входного файла (по умолчанию `input.txt`).
- `-o` или `--output` - указывает имя выходного файла (по умолчанию `output.bin`).
- `-d` или `--decode` - флаг, указывающий на необходимость декодирования (распаковки) данных.

### Примеры

1. **Сжатие файла:**

   ```bash
   java HuffmanCoding -i input.txt -o output.bin
   ```

   В этом примере программа сожмет содержимое `input.txt` и сохранит результат в `output.bin`.

2. **Декодирование файла:**

   ```bash
   java HuffmanCoding -i output.bin -o decompressed.txt -d
   ```

   В этом примере программа распакует содержимое `output.bin` и сохранит результат в `decompressed.txt`.

## Обработка ошибок

- Если не указано имя входного файла, программа выведет сообщение об ошибке: `Ошибка: Не указано имя входного файла.`
- Если не указано имя выходного файла, программа выведет сообщение об ошибке: `Ошибка: Не указано имя выходного файла.`
- Если передан неизвестный аргумент, программа выведет сообщение: `Неизвестный аргумент: <аргумент>`

## Структура записываемых закодированных данных

1. **Размер закодированных данных**:
   - размер `encodedData`:
     ```java
     dos.writeInt(encodedData.size());
     ```

2. **Закодированные данные**:
   - `encodedData` преобразуем в `byteArray`:
     ```java
     for (int i = 0; i < encodedData.size(); i++) {
         if (encodedData.get(i)) {
             byteArray[i / 8] |= (1 << (7 - (i % 8)));
         }
     }
     dos.write(byteArray);
     ```

3. **Коды Хаффмана**:
   - Для каждого кода записывается:
     - Символ (1 байт):
       ```java
       dos.writeByte(entry.getKey());
       ```
     - Размер кода кодирования (1 байт):
       ```java
       dos.writeByte(tempCode.size());
       ```
     - код символа `tempByteArray`:
       ```java
        for (int i = 0; i < tempCode.size(); i++) {
            if (tempCode.get(i)) { 
                tempByteArray[i / 8] |= (1 << (7 - (i % 8)));
            }
        }
       dos.write(tempByteArray);
       ```

Структура записываемых данных:
1. `int` (4 байта) - размер `encodedData`
2. `byte[]` - закодированные данные
3. Для каждого символа:
   - `byte` (1 байт) - символ
   - `short` (1 байт) - размер кода
   - `byte[]` - код символа
