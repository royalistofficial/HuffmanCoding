import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.FileHandler;
import java.util.logging.SimpleFormatter;
import java.io.*;
import java.util.*;

public class HuffmanCoding {

    private static final Logger logger = Logger.getLogger(HuffmanCoding.class.getName());
    
    static {
        try {
            logger.setUseParentHandlers(false);

            FileHandler fileHandler = new FileHandler("logger.log", true);
            fileHandler.setFormatter(new SimpleFormatter());

            logger.addHandler(fileHandler);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private static class Node {
        Byte value;
        int freq;
        Node left;
        Node right;
    
        public Node(Byte value, int freq) {
            this.value = value;
            this.freq = freq;
            this.left = null;
            this.right = null;
        }

        public int getFreq() {
            return freq;
        }
    }

    public static void generateCodes(Node root, List<Boolean> code, Map<Byte, List<Boolean>> huffmanCodes) {
        if (root == null) {
            return;
        }
        if (root.left == null && root.right == null) {
            huffmanCodes.put(root.value, new ArrayList<>(code));
        } else {
            code.add(false);
            generateCodes(root.left, code, huffmanCodes);
            code.remove(code.size() - 1); 
            
            code.add(true);
            generateCodes(root.right, code, huffmanCodes);
            code.remove(code.size() - 1);
        }
    }

    public static Node buildHuffmanTree(Map<Byte, Integer> frequencyMap) {
        logger.info("Начало buildHuffmanTree");
        PriorityQueue<Node> nodes = new PriorityQueue<>(Comparator.comparingInt(Node::getFreq));

        for (Map.Entry<Byte, Integer> entry : frequencyMap.entrySet()) {
            nodes.offer(new Node(entry.getKey(), entry.getValue()));
        }

        while (nodes.size() > 1) {
            Node minNode1 = nodes.poll();
            Node minNode2 = nodes.poll();

            Node merged = new Node(null, minNode1.freq + minNode2.freq);
            merged.left = minNode1;
            merged.right = minNode2;

            nodes.offer(merged);
        }

        logger.info("Конец buildHuffmanTree");
        return nodes.poll();
    }

    public static List<Boolean> huffmanEncode(byte[] data, Map<Byte, List<Boolean>> huffmanCodes) {
        logger.info("Начало huffmanEncode");
        List<Boolean> encodedData = new ArrayList<>(data.length * 8);
        for (byte b : data) {
            List<Boolean> code = huffmanCodes.get(b);
            if (code != null) {
                encodedData.addAll(code);
            }
        }
        logger.info("Конец huffmanEncode");
        return encodedData;
    }
    
    public static byte[] huffmanDecode(List<Boolean> encodedData, Map<Byte, List<Boolean>> huffmanCodes) {
        logger.info("Начало huffmanDecode");
    
        Map<List<Boolean>, Byte> reverseHuffmanCodes = new HashMap<>();
        for (Map.Entry<Byte, List<Boolean>> entry : huffmanCodes.entrySet()) {
            reverseHuffmanCodes.put(entry.getValue(), entry.getKey());
        }
        logger.info("Конец создания reverseHuffmanCodes");
    
        List<Byte> outputList = new ArrayList<>();
        List<Boolean> currentCode = new ArrayList<>();
    
        for (Boolean bit : encodedData) {
            currentCode.add(bit);
            Byte decodedByte = reverseHuffmanCodes.get(currentCode);
            if (decodedByte != null) {
                outputList.add(decodedByte);
                currentCode.clear();
            }
        }
        logger.info("Конец создания outputList");
    
        byte[] outputArray = new byte[outputList.size()];
        for (int i = 0; i < outputList.size(); i++) {
            outputArray[i] = outputList.get(i);
        }
    
        logger.info("Конец huffmanDecode");
        return outputArray;
    }
    
    public static void readAndCompress(String inputFile, String outputFile) {
        logger.info("Начало readAndCompress");
        File file = new File(inputFile);
        byte[] data = new byte[(int) file.length()];

        try (FileInputStream fis = new FileInputStream(file)) {
            fis.read(data);
        } catch (IOException e) {
            logger.severe("Ошибка при чтении файла: " + e.getMessage());
            return;
        }
        // logger.info("data: " + new String(data));
    
        Map<Byte, Integer> frequencyMap = new HashMap<>();
        for (byte b : data) {
            frequencyMap.put(b, frequencyMap.getOrDefault(b, 0) + 1);
        }
        // logger.info("frequencyMap: " + frequencyMap);

        Node huffmanTree = buildHuffmanTree(frequencyMap);

        Map<Byte, List<Boolean>> huffmanCodes = new HashMap<>();
        logger.info("Начало generateCodes");
        generateCodes(huffmanTree, new ArrayList<>(), huffmanCodes);    
        logger.info("Конец generateCodes");    
        // logger.info("huffmanCodes: " + huffmanCodes);
        
        List<Boolean> encodedData = huffmanEncode(data, huffmanCodes);
        // logger.info("encodedData: " + encodedData);  

        logger.info("Начало записи в файл");
        try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(outputFile))) {
            byte[] byteArray = new byte[(encodedData.size() + 7) / 8];
            // logger.info("encodedData.size(): " + encodedData.size());
            // logger.info("byteArray.length: " + byteArray.length);
        
            for (int i = 0; i < encodedData.size(); i++) {
                if (encodedData.get(i)) {
                    byteArray[i / 8] |= (1 << (7 - (i % 8)));
                }
            }
            dos.writeInt(encodedData.size());
            dos.write(byteArray); 
        
            for (Map.Entry<Byte, List<Boolean>> entry : huffmanCodes.entrySet()) {
                List<Boolean> tempCode = entry.getValue();
                byte[] tempByteArray = new byte[(tempCode.size() + 7) / 8];

                // logger.info("tempCode.size(): " + tempCode.size());
                // logger.info("tempByteArray.length: " + tempByteArray.length);
        
                for (int i = 0; i < tempCode.size(); i++) {
                    if (tempCode.get(i)) { 
                        tempByteArray[i / 8] |= (1 << (7 - (i % 8)));
                    }
                }
                dos.writeByte(entry.getKey());
                dos.writeByte(tempCode.size());
                dos.write(tempByteArray);
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Ошибка при записи в файл: " + e.getMessage(), e);
            return;
        }
        logger.info("Конец записи в файл");

    }

    public static void readAndDecompressToFile(String inputFile, String outputFile) {
        logger.info("Начало readAndDecompressToFile");
        List<Boolean> encodedData = new ArrayList<>();
        Map<Byte, List<Boolean>> huffmanCodes = new HashMap<>();
        
        try (DataInputStream dis = new DataInputStream(new FileInputStream(inputFile))) {
            int encodedLength = dis.readInt();
            byte[] byteArray = new byte[(encodedLength + 7) / 8];
        
            dis.readFully(byteArray);
            int count = 0;
            for (byte b : byteArray) {
                for (int i = 7; i >= 0; i--) {
                    if (count < encodedLength){
                        encodedData.add(((b >> i) & 1) == 1);
                        count ++;
                    }
                }
            }
            logger.info("Создано encodedData");
        
            while (dis.available() > 0) {
                byte key = dis.readByte();
                int codeLength = dis.readByte();
                byte[] tempByteArray = new byte[(codeLength + 7) / 8];
                dis.readFully(tempByteArray);
        
                List<Boolean> huffmanCode = new ArrayList<>();
                count = 0;
                for (byte b : tempByteArray) {
                    for (int i = 7; i >= 0; i--) {
                        if (count < codeLength){
                            huffmanCode.add(((b >> i) & 1) == 1);
                            count ++;
                        }
                    }
                }
                huffmanCodes.put(key, huffmanCode.subList(0, codeLength));
            }
            logger.info("Создано huffmanCodes");
        } catch (IOException e) {
            logger.severe("Ошибка при чтении файла: " + e.getMessage());
            return;
        }
        
        byte[] decodedData = huffmanDecode(encodedData, huffmanCodes);
        // logger.info("Раскодированные данные: " + new String(decodedData));

        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            fos.write(decodedData);
        } catch (IOException e) {
            logger.severe("Ошибка при записи в файл: " + e.getMessage());
        }
    }
    public static void main(String[] args) {
        // logger.setLevel(Level.SEVERE);
        String inputFile = "input.txt";
        String outputFile = "output.bin";
        boolean decode = false;

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-i":
                case "--input":
                    if (i + 1 < args.length) {
                        inputFile = args[++i];
                    } else {
                        System.err.println("Ошибка: Не указано имя входного файла.");
                    }
                    break;
                case "-o":
                case "--output":
                    if (i + 1 < args.length) {
                        outputFile = args[++i];
                    } else {
                        System.err.println("Ошибка: Не указано имя выходного файла.");
                    }
                    break;
                case "-d":
                case "--decode":
                        decode = true;
                        break;
                default:
                    System.err.println("Неизвестный аргумент: " + args[i]);
                    break;
            }
        }


        if (decode) {
            readAndDecompressToFile(inputFile, outputFile);
        } else {
            readAndCompress(inputFile, outputFile);
        }
    }
}
