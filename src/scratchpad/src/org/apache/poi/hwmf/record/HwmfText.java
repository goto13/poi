/* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */

package org.apache.poi.hwmf.record;

import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.text.AttributedString;

import org.apache.poi.hwmf.draw.HwmfDrawProperties;
import org.apache.poi.hwmf.draw.HwmfGraphics;
import org.apache.poi.hwmf.record.HwmfMisc.WmfSetMapMode;
import org.apache.poi.util.BitField;
import org.apache.poi.util.BitFieldFactory;
import org.apache.poi.util.LittleEndianConsts;
import org.apache.poi.util.LittleEndianInputStream;
import org.apache.poi.util.LocaleUtil;
import org.apache.poi.util.RecordFormatException;

public class HwmfText {

    /**
     * The META_SETTEXTCHAREXTRA record defines inter-character spacing for text justification in the 
     * playback device context. Spacing is added to the white space between each character, including
     * break characters, when a line of justified text is output.
     */
    public static class WmfSetTextCharExtra implements HwmfRecord {
        
        /**
         * A 16-bit unsigned integer that defines the amount of extra space, in
         * logical units, to be added to each character. If the current mapping mode is not MM_TEXT,
         * this value is transformed and rounded to the nearest pixel. For details about setting the
         * mapping mode, see META_SETMAPMODE
         */
        private int charExtra;
        
        @Override
        public HwmfRecordType getRecordType() {
            return HwmfRecordType.setTextCharExtra;
        }
        
        @Override
        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            charExtra = leis.readUShort();
            return LittleEndianConsts.SHORT_SIZE;
        }

        @Override
        public void draw(HwmfGraphics ctx) {

        }
    }
    
    /**
     * The META_SETTEXTCOLOR record defines the text foreground color in the playback device context.
     */
    public static class WmfSetTextColor implements HwmfRecord {
        
        private HwmfColorRef colorRef;
        
        @Override
        public HwmfRecordType getRecordType() {
            return HwmfRecordType.setTextColor;
        }
        
        @Override
        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            colorRef = new HwmfColorRef();
            return colorRef.init(leis);
        }

        @Override
        public void draw(HwmfGraphics ctx) {
            ctx.getProperties().setTextColor(colorRef);
        }
    }
    
    /**
     * The META_SETTEXTJUSTIFICATION record defines the amount of space to add to break characters
     * in a string of justified text.
     */
    public static class WmfSetTextJustification implements HwmfRecord {
        
        /**
         * A 16-bit unsigned integer that specifies the number of space characters in the line.
         */
        private int breakCount;
        
        /**
         * A 16-bit unsigned integer that specifies the total extra space, in logical
         * units, to be added to the line of text. If the current mapping mode is not MM_TEXT, the value
         * identified by the BreakExtra member is transformed and rounded to the nearest pixel. For
         * details about setting the mapping mode, see {@link WmfSetMapMode}.
         */
        private int breakExtra;
        
        @Override
        public HwmfRecordType getRecordType() {
            return HwmfRecordType.setBkColor;
        }
        
        @Override
        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            breakCount = leis.readUShort();
            breakExtra = leis.readUShort();
            return 2*LittleEndianConsts.SHORT_SIZE;
        }

        @Override
        public void draw(HwmfGraphics ctx) {

        }
    }
    
    /**
     * The META_TEXTOUT record outputs a character string at the specified location by using the font,
     * background color, and text color that are defined in the playback device context.
     */
    public static class WmfTextOut implements HwmfRecord {
        /**
         * A 16-bit signed integer that defines the length of the string, in bytes, pointed to by String.
         */
        private int stringLength;
        /**
         * The size of this field MUST be a multiple of two. If StringLength is an odd
         * number, then this field MUST be of a size greater than or equal to StringLength + 1.
         * A variable-length string that specifies the text to be drawn.
         * The string does not need to be null-terminated, because StringLength specifies the
         * length of the string.
         * The string is written at the location specified by the XStart and YStart fields.
         */
        private String text;
        /**
         * A 16-bit signed integer that defines the vertical (y-axis) coordinate, in logical
         * units, of the point where drawing is to start.
         */
        private int yStart;
        /**
         * A 16-bit signed integer that defines the horizontal (x-axis) coordinate, in
         * logical units, of the point where drawing is to start.
         */
        private int xStart;  
        
        @Override
        public HwmfRecordType getRecordType() {
            return HwmfRecordType.textOut;
        }
        
        @Override
        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            stringLength = leis.readShort();
            byte buf[] = new byte[stringLength+(stringLength&1)];
            leis.readFully(buf);
            text = new String(buf, 0, stringLength, LocaleUtil.CHARSET_1252).trim();
            yStart = leis.readShort();
            xStart = leis.readShort();
            return 3*LittleEndianConsts.SHORT_SIZE+buf.length;
        }

        @Override
        public void draw(HwmfGraphics ctx) {
            Rectangle2D bounds = new Rectangle2D.Double(xStart, yStart, 0, 0);
            ctx.drawString(text, bounds);
        }
    }
    
    /**
     * The META_EXTTEXTOUT record outputs text by using the font, background color, and text color that
     * are defined in the playback device context. Optionally, dimensions can be provided for clipping,
     * opaquing, or both.
     */
    public static class WmfExtTextOut implements HwmfRecord {

        /**
         * A 16-bit signed integer that defines the y-coordinate, in logical units, where the 
        text string is to be located.
         */
        private int y;  
        /**
         * A 16-bit signed integer that defines the x-coordinate, in logical units, where the 
        text string is to be located.
         */
        private int x;  
        /**
         * A 16-bit signed integer that defines the length of the string.
         */
        private int stringLength;
        /**
         * A 16-bit unsigned integer that defines the use of the application-defined 
         * rectangle. This member can be a combination of one or more values in the 
         * ExtTextOutOptions Flags:
         * 
         * ETO_OPAQUE (0x0002):
         * Indicates that the background color that is defined in the playback device context 
         * SHOULD be used to fill the rectangle.
         * 
         * ETO_CLIPPED (0x0004):
         * Indicates that the text SHOULD be clipped to the rectangle.
         * 
         * ETO_GLYPH_INDEX (0x0010):
         * Indicates that the string to be output SHOULD NOT require further processing 
         * with respect to the placement of the characters, and an array of character 
         * placement values SHOULD be provided. This character placement process is 
         * useful for fonts in which diacritical characters affect character spacing.
         * 
         * ETO_RTLREADING (0x0080):
         * Indicates that the text MUST be laid out in right-to-left reading order, instead of 
         * the default left-to-right order. This SHOULD be applied only when the font that is 
         * defined in the playback device context is either Hebrew or Arabic. <37>
         * 
         * ETO_NUMERICSLOCAL (0x0400):
         * Indicates that to display numbers, digits appropriate to the locale SHOULD be 
         * used.
         * 
         * ETO_NUMERICSLATIN (0x0800):
         * Indicates that to display numbers, European digits SHOULD be used. <39>
         * 
         * ETO_PDY (0x2000):
         * Indicates that both horizontal and vertical character displacement values 
         * SHOULD be provided.
         */
        private int fwOpts;
        /**
         * An optional 8-byte Rect Object (section 2.2.2.18) that defines the 
         * dimensions, in logical coordinates, of a rectangle that is used for clipping, opaquing, or both.
         * 
         * The corners are given in the order left, top, right, bottom.
         * Each value is a 16-bit signed integer that defines the coordinate, in logical coordinates, of 
         * the upper-left corner of the rectangle
         */
        private int left,top,right,bottom;
        /**
         * A variable-length string that specifies the text to be drawn. The string does 
         * not need to be null-terminated, because StringLength specifies the length of the string. If 
         * the length is odd, an extra byte is placed after it so that the following member (optional Dx) is 
         * aligned on a 16-bit boundary.
         */
        private String text;
        /**
         * An optional array of 16-bit signed integers that indicate the distance between 
         * origins of adjacent character cells. For example, Dx[i] logical units separate the origins of 
         * character cell i and character cell i + 1. If this field is present, there MUST be the same 
         * number of values as there are characters in the string.
         */
        private int dx[];
        
        @Override
        public HwmfRecordType getRecordType() {
            return HwmfRecordType.extTextOut;
        }
        
        @Override
        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            // -6 bytes of record function and length header
            final int remainingRecordSize = (int)(recordSize-6);
            
            y = leis.readShort();
            x = leis.readShort();
            stringLength = leis.readShort();
            fwOpts = leis.readUShort();
            
            int size = 4*LittleEndianConsts.SHORT_SIZE;
            
            if (fwOpts != 0 && size+8<=remainingRecordSize) {
                // the bounding rectangle is optional and only read when fwOpts are given
                left = leis.readShort();
                top = leis.readShort();
                right = leis.readShort();
                bottom = leis.readShort();
                size += 4*LittleEndianConsts.SHORT_SIZE;
            }
            
            byte buf[] = new byte[stringLength+(stringLength&1)];
            leis.readFully(buf);
            text = new String(buf, 0, stringLength, LocaleUtil.CHARSET_1252);
            size += buf.length;
            
            if (size < remainingRecordSize) {
                if (size + stringLength*LittleEndianConsts.SHORT_SIZE < remainingRecordSize) {
                    throw new RecordFormatException("can't read Dx array - given recordSize doesn't contain enough values for string length "+stringLength);
                }
                
                dx = new int[stringLength];
                for (int i=0; i<dx.length; i++) {
                    dx[i] = leis.readShort();
                }
                size += dx.length*LittleEndianConsts.SHORT_SIZE;
            }
            
            return size;
        }

        @Override
        public void draw(HwmfGraphics ctx) {

        }
    }
    
    public enum HwmfTextAlignment {
        LEFT,
        RIGHT,
        CENTER;
    }
    
    public enum HwmfTextVerticalAlignment {
        TOP,
        BOTTOM,
        BASELINE;
    }
    
    /**
     * The META_SETTEXTALIGN record defines text-alignment values in the playback device context.
     */
    public static class WmfSetTextAlign implements HwmfRecord {
        
        // ***********************************************************************************
        // TextAlignmentMode Flags:
        // ***********************************************************************************
        
        /** 
         * The drawing position in the playback device context MUST NOT be updated after each
         * text output call. The reference point MUST be passed to the text output function.
         */
        @SuppressWarnings("unused")
        private static final BitField TA_NOUPDATECP = BitFieldFactory.getInstance(0x0000);
        
        /**
         * The reference point MUST be on the left edge of the bounding rectangle.
         */
        @SuppressWarnings("unused")
        private static final BitField TA_LEFT = BitFieldFactory.getInstance(0x0000);
        
        /**
         * The reference point MUST be on the top edge of the bounding rectangle.
         */
        @SuppressWarnings("unused")
        private static final BitField TA_TOP = BitFieldFactory.getInstance(0x0000);
        
        /**
         * The drawing position in the playback device context MUST be updated after each text
         * output call. It MUST be used as the reference point.
         */
        @SuppressWarnings("unused")
        private static final BitField TA_UPDATECP = BitFieldFactory.getInstance(0x0001);
        
        /**
         * The reference point MUST be on the right edge of the bounding rectangle.
         */
        private static final BitField TA_RIGHT = BitFieldFactory.getInstance(0x0002);
        
        /**
         * The reference point MUST be aligned horizontally with the center of the bounding
         * rectangle.
         */
        private static final BitField TA_CENTER = BitFieldFactory.getInstance(0x0006);
        
        /**
         * The reference point MUST be on the bottom edge of the bounding rectangle.
         */
        private static final BitField TA_BOTTOM = BitFieldFactory.getInstance(0x0008);
        
        /**
         * The reference point MUST be on the baseline of the text.
         */
        private static final BitField TA_BASELINE = BitFieldFactory.getInstance(0x0018);
        
        /**
         * The text MUST be laid out in right-to-left reading order, instead of the default
         * left-to-right order. This SHOULD be applied only when the font that is defined in the
         * playback device context is either Hebrew or Arabic.
         */
        @SuppressWarnings("unused")
        private static final BitField TA_RTLREADING = BitFieldFactory.getInstance(0x0100);
        
        // ***********************************************************************************
        // VerticalTextAlignmentMode Flags (e.g. for Kanji fonts)
        // ***********************************************************************************
        
        /**
         * The reference point MUST be on the top edge of the bounding rectangle.
         */
        @SuppressWarnings("unused")
        private static final BitField VTA_TOP = BitFieldFactory.getInstance(0x0000);
        
        /**
         * The reference point MUST be on the right edge of the bounding rectangle.
         */
        @SuppressWarnings("unused")
        private static final BitField VTA_RIGHT = BitFieldFactory.getInstance(0x0000);
        
        /**
         * The reference point MUST be on the bottom edge of the bounding rectangle.
         */
        private static final BitField VTA_BOTTOM = BitFieldFactory.getInstance(0x0002);
        
        /**
         * The reference point MUST be aligned vertically with the center of the bounding
         * rectangle.
         */
        private static final BitField VTA_CENTER = BitFieldFactory.getInstance(0x0006);
        
        /**
         * The reference point MUST be on the left edge of the bounding rectangle.
         */
        private static final BitField VTA_LEFT = BitFieldFactory.getInstance(0x0008);
        
        /**
         * The reference point MUST be on the baseline of the text.
         */
        private static final BitField VTA_BASELINE = BitFieldFactory.getInstance(0x0018);
        
        /**
         * A 16-bit unsigned integer that defines text alignment.
         * This value MUST be a combination of one or more TextAlignmentMode Flags
         * for text with a horizontal baseline, and VerticalTextAlignmentMode Flags
         * for text with a vertical baseline.
         */
        private int textAlignmentMode;
        
        @Override
        public HwmfRecordType getRecordType() {
            return HwmfRecordType.setTextAlign;
        }
        
        @Override
        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            textAlignmentMode = leis.readUShort();
            return LittleEndianConsts.SHORT_SIZE;
        }

        @Override
        public void draw(HwmfGraphics ctx) {
            HwmfDrawProperties props = ctx.getProperties();
            if (TA_CENTER.isSet(textAlignmentMode)) {
                props.setTextAlignLatin(HwmfTextAlignment.CENTER);
            } else if (TA_RIGHT.isSet(textAlignmentMode)) {
                props.setTextAlignLatin(HwmfTextAlignment.RIGHT);
            } else {
                props.setTextAlignLatin(HwmfTextAlignment.LEFT);
            }

            if (VTA_CENTER.isSet(textAlignmentMode)) {
                props.setTextAlignAsian(HwmfTextAlignment.CENTER);
            } else if (VTA_LEFT.isSet(textAlignmentMode)) {
                props.setTextAlignAsian(HwmfTextAlignment.LEFT);
            } else {
                props.setTextAlignAsian(HwmfTextAlignment.RIGHT);
            }

            if (TA_BASELINE.isSet(textAlignmentMode)) {
                props.setTextVAlignLatin(HwmfTextVerticalAlignment.BASELINE);
            } else if (TA_BOTTOM.isSet(textAlignmentMode)) {
                props.setTextVAlignLatin(HwmfTextVerticalAlignment.BOTTOM);
            } else {
                props.setTextVAlignLatin(HwmfTextVerticalAlignment.TOP);
            }

            if (VTA_BASELINE.isSet(textAlignmentMode)) {
                props.setTextVAlignAsian(HwmfTextVerticalAlignment.BASELINE);
            } else if (VTA_BOTTOM.isSet(textAlignmentMode)) {
                props.setTextVAlignAsian(HwmfTextVerticalAlignment.BOTTOM);
            } else {
                props.setTextVAlignAsian(HwmfTextVerticalAlignment.TOP);
            }
        }
    }
    
    public static class WmfCreateFontIndirect implements HwmfRecord, HwmfObjectTableEntry {
        private HwmfFont font;
        
        @Override
        public HwmfRecordType getRecordType() {
            return HwmfRecordType.createFontIndirect;
        }
        
        @Override
        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            font = new HwmfFont();
            return font.init(leis);
        }

        @Override
        public void draw(HwmfGraphics ctx) {
            ctx.addObjectTableEntry(this);
        }
        
        @Override
        public void applyObject(HwmfGraphics ctx) {
            ctx.getProperties().setFont(font);
        }
    }
}
