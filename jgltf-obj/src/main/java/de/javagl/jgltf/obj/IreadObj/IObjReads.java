package de.javagl.jgltf.obj.IreadObj;

import de.javagl.obj.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.StringTokenizer;

public class IObjReads {
    public static Obj read(InputStream inputStream) throws IOException {
        return (Obj) read((InputStream) inputStream, Objs.create());
    }

    public static <T extends WritableObj> T read(InputStream inputStream, T output) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.US_ASCII));
        return readImpl(reader, output);
    }

    public static Obj read(Reader reader) throws IOException {
        return (Obj) read((Reader) reader, Objs.create());
    }

    public static <T extends WritableObj> T read(Reader reader, T output) throws IOException {
        return reader instanceof BufferedReader ? readImpl((BufferedReader) reader, output) : readImpl(new BufferedReader(reader), output);
    }

    private static <T extends WritableObj> T readImpl(BufferedReader reader, T output) throws IOException {
        IObjFaceParser objFaceParser = new IObjFaceParser();
        int vertexCounter = 0;
        int texCoordCounter = 0;
        int normalCounter = 0;
        String o = "";
        while (true) {
            String line = reader.readLine();
            if (line == null) {
                break;
            }

            boolean finished;
            String nextLine;
            for (finished = false; line.endsWith("\\"); line = line + " " + nextLine) {
                line = line.substring(0, line.length() - 2);
                nextLine = reader.readLine();
                if (nextLine == null) {
                    finished = true;
                    break;
                }
            }
            if (finished) {
                break;
            }
            StringTokenizer st = new StringTokenizer(line);
            if (st.hasMoreTokens()) {
                String identifier = st.nextToken().toLowerCase();
                if (identifier.equals("o")) {
                    String s = st.nextToken();
                    o = s;
                } else if (identifier.equals("v")) {
                    output.addVertex(readFloatTuple(st));
                    ++vertexCounter;
                } else if (identifier.equals("vt")) {
                    output.addTexCoord(readFloatTuple(st));
                    ++texCoordCounter;
                } else if (identifier.equals("vn")) {
                    output.addNormal(readFloatTuple(st));
                    ++normalCounter;
                } else {
                    String s;
                    if (identifier.equals("mtllib")) {
                        s = line.substring(6).trim();
                        output.setMtlFileNames(Collections.singleton(s));
                    } else if (identifier.equals("usemtl")) {
                        s = line.substring(6).trim();
                        output.setActiveMaterialGroupName(o+"_"+s);
                    } else if (identifier.equals("g")) {
                        s = line.substring(1).trim();
                        String[] groupNames = readStrings(s);
                        output.setActiveGroupNames(Arrays.asList(groupNames));
                    } else if (identifier.equals("f")) {
                        objFaceParser.parse(line);
                        int[] v = objFaceParser.getVertexIndices();
                        int[] vt = objFaceParser.getTexCoordIndices();
                        int[] vn = objFaceParser.getNormalIndices();
                        makeIndicesAbsolute(v, vertexCounter);
                        makeIndicesAbsolute(vt, texCoordCounter);
                        makeIndicesAbsolute(vn, normalCounter);
                        output.addFace(ObjFaces.create(v, vt, vn));
                    }
                }
            }
        }

        return output;
    }

    private static void makeIndicesAbsolute(int[] array, int count) {
        if (array != null) {
            for (int i = 0; i < array.length; ++i) {
                if (array[i] < 0) {
                    array[i] += count;
                } else {
                    --array[i];
                }
            }

        }
    }

    private static String[] readStrings(String input) {
        StringTokenizer st = new StringTokenizer(input);
        ArrayList tokens = new ArrayList();

        while (st.hasMoreTokens()) {
            tokens.add(st.nextToken());
        }

        return (String[]) tokens.toArray(new String[tokens.size()]);
    }

    private static FloatTuple readFloatTuple(StringTokenizer st) throws IOException {
        float x = parse(st.nextToken());
        if (st.hasMoreTokens()) {
            float y = parse(st.nextToken());
            if (st.hasMoreTokens()) {
                float z = parse(st.nextToken());
                if (st.hasMoreTokens()) {
                    float w = parse(st.nextToken());
                    return FloatTuples.create(x, y, z, w);
                } else {
                    return FloatTuples.create(x, y, z);
                }
            } else {
                return FloatTuples.create(x, y);
            }
        } else {
            return FloatTuples.create(x);
        }
    }

    private static float parse(String s) throws IOException {
        try {
            return Float.parseFloat(s);
        } catch (NumberFormatException var2) {
            throw new IOException(var2);
        }
    }

    private IObjReads() {
    }
}
