package de.javagl.jgltf.obj.IreadObj;

import java.io.IOException;
import java.util.Arrays;

public class IObjFaceParser {
    private static final int INITIAL_BUFFER_SIZE = 6;
    private int[] vertexIndexBuffer = new int[6];
    private int[] texCoordIndexBuffer = new int[6];
    private int[] normalIndexBuffer = new int[6];
    private boolean foundTexCoordIndices = false;
    private boolean foundNormalIndices = false;
    private int vertexCounter = 0;
    private int idx = 0;
    private char[] lineData;

    IObjFaceParser() {
    }

    void parse(String line) throws IOException {
        this.parseLine(line);
    }

    int[] getVertexIndices() {
        return Arrays.copyOf(this.vertexIndexBuffer, this.vertexCounter);
    }

    int[] getTexCoordIndices() {
        return this.foundTexCoordIndices ? Arrays.copyOf(this.texCoordIndexBuffer, this.vertexCounter) : null;
    }

    int[] getNormalIndices() {
        return this.foundNormalIndices ? Arrays.copyOf(this.normalIndexBuffer, this.vertexCounter) : null;
    }

    void parseLine(String line) throws IOException {
        this.foundTexCoordIndices = false;
        this.foundNormalIndices = false;
        this.vertexCounter = 0;
        this.idx = 0;
        this.lineData = line.toCharArray();
        this.skipSpaces();
        if (!this.endOfInput()) {
            if (this.lineData[this.idx] != 'f' && this.lineData[this.idx] != 'F') {
                throw new IOException("Expected 'f' or 'F', but found '" + this.lineData[this.idx] + " in \"" + line + "\"");
            } else {
                ++this.idx;
                int count = 0;

                while(true) {
                    this.skipSpaces();
                    if (this.endOfInput()) {
                        break;
                    }

                    int vertexIndex = this.parseNonzeroInt();
                    if (vertexIndex == 0) {
                        throw new IOException("Could not read vertex index in \"" + line + "\"");
                    }

                    if (count >= this.vertexIndexBuffer.length) {
                        this.vertexIndexBuffer = Arrays.copyOf(this.vertexIndexBuffer, count + 1);
                        this.texCoordIndexBuffer = Arrays.copyOf(this.texCoordIndexBuffer, count + 1);
                        this.normalIndexBuffer = Arrays.copyOf(this.normalIndexBuffer, count + 1);
                    }

                    if (vertexIndex != 0) {
                        this.vertexIndexBuffer[count] = vertexIndex;
                    }

                    this.vertexCounter = count + 1;
                    this.skipSpaces();
                    if (this.endOfInput()) {
                        break;
                    }

                    if (this.lineData[this.idx] == '/') {
                        ++this.idx;
                        this.skipSpaces();
                        if (this.endOfInput()) {
                            throw new IOException("Unexpected end of input after '/' in  \"" + line + "\"");
                        }

                        int texCoordIndex = this.parseNonzeroInt();
                        if (texCoordIndex != 0) {
                            this.texCoordIndexBuffer[count] = texCoordIndex;
                            this.foundTexCoordIndices = true;
                        }

                        this.skipSpaces();
                        if (this.endOfInput()) {
                            break;
                        }

                        if (this.lineData[this.idx] == '/') {
                            ++this.idx;
                            this.skipSpaces();
                            if (this.endOfInput()) {
                                throw new IOException("Unexpected end of input after '/' in  \"" + line + "\"");
                            }

                            int normalIndex = this.parseNonzeroInt();
                            if (normalIndex == 0) {
                                throw new IOException("Could not read normal index from \"" + line + "\"");
                            }

                            this.foundNormalIndices = true;
                            if (normalIndex != 0) {
                                this.normalIndexBuffer[count] = normalIndex;
                            }
                        }
                    }

                    ++count;
                }

            }
        }
    }

    private boolean endOfInput() {
        return this.idx >= this.lineData.length;
    }

    private void skipSpaces() {
        while(!this.endOfInput() && this.lineData[this.idx] == ' ') {
            ++this.idx;
        }

    }

    private int parseNonzeroInt() {
        int parsedInt = 0;
        boolean negate = false;
        if (this.lineData[this.idx] == '-') {
            negate = true;
            ++this.idx;
            this.skipSpaces();
            if (this.endOfInput()) {
                return 0;
            }
        }

        if (this.lineData[this.idx] >= '0' && this.lineData[this.idx] <= '9') {
            parsedInt = this.lineData[this.idx] - 48;
            ++this.idx;

            while(!this.endOfInput() && this.lineData[this.idx] >= '0' && this.lineData[this.idx] <= '9') {
                parsedInt *= 10;
                parsedInt += this.lineData[this.idx] - 48;
                ++this.idx;
            }
        }

        return negate ? -parsedInt : parsedInt;
    }
}
