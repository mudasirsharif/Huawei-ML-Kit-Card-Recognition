/*
 * Copyright 2020. Huawei Technologies Co., Ltd. All rights reserved.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.dtse.mlkitdemo.MLUtils;

import android.graphics.Point;
import android.graphics.Rect;
import android.util.Log;

import com.huawei.hms.mlsdk.text.MLText;

import java.util.ArrayList;
import java.util.List;

/**
 * Post-processing plug-in for Emirates ID front and back side
 */
public class EmiratesIDCardProcess {
    private static final String TAG = EmiratesIDCardProcess.class.getSimpleName();

    private final MLText text;
    private final Boolean isFrontSide;

    public EmiratesIDCardProcess(MLText text, Boolean isFrontSide) {
        this.text = text;
        this.isFrontSide = isFrontSide;
    }

    // Re-encapsulate the return result of OCR in Block.
    class BlockItem {
        final String text;
        final Rect rect;

        BlockItem(String text, Rect rect) {
            this.text = text;
            this.rect = rect;
        }
    }

    public EmiratesIDCardResult getResult() {
        List<MLText.Block> blocks = this.text.getBlocks();
        if (blocks.isEmpty()) {
            Log.i(EmiratesIDCardProcess.TAG, "PassCardProcess::getResult blocks is empty");
            return null;
        }
        ArrayList<BlockItem> originItems = this.getOriginItems(blocks);

        String idNumber = "";
        String name = "";
        String nationality = "";
        boolean idNumberFlag = false;
        boolean nameFlag = false;
        boolean nationalityFlag = false;

        String dob = "";
        String gender = "";
        String expiryDate = "";
        boolean dobFlag = false;
        boolean genderFlag = false;
        boolean expiryDateFlag = false;


        if (isFrontSide) {

            for (BlockItem item : originItems) {
                String tempStr = item.text;

                if (!idNumberFlag) {
                    String result = PostProcessingUtils.tryGetIdNumber(tempStr);
                    if (!result.isEmpty()) {
                        idNumber = result;
                        idNumberFlag = true;
                    }
                }
                if (!nameFlag) {
                    String result = PostProcessingUtils.tryGetName(tempStr);
                    if (!result.isEmpty()) {
                        name = result;
                        nameFlag = true;
                    }
                }
                if (!nationalityFlag) {
                    String result = PostProcessingUtils.tryGetNationality(tempStr);
                    if (!result.isEmpty()) {
                        nationality = result;
                        nationalityFlag = true;
                    }
                }
            }
            return new EmiratesIDCardResult(idNumber, name, nationality, null,
                    null, null);
        } else {

            for (BlockItem item : originItems) {
                String tempStr = item.text;

                if (!dobFlag) {
                    String result = PostProcessingUtils.tryGetDob(tempStr);
                    if (!result.isEmpty()) {
                        dob = result;
                        dobFlag = true;
                    }
                }
                if (!genderFlag) {
                    String result = PostProcessingUtils.tryGetGender(tempStr);
                    if (!result.isEmpty()) {
                        gender = result;
                        genderFlag = true;
                    }
                }
                if (!expiryDateFlag) {
                    String result = PostProcessingUtils.tryGetExpiryDate(tempStr);
                    if (!result.isEmpty()) {
                        expiryDate = result;
                        expiryDateFlag = true;
                    }
                }
            }
            return new EmiratesIDCardResult(null, null,
                    null, dob, gender, expiryDate);
        }
    }

    /**
     * Transform the detected text into BlockItem structure.
     *
     * @param blocks
     * @return
     */
    private ArrayList<BlockItem> getOriginItems(List<MLText.Block> blocks) {
        ArrayList<BlockItem> originItems = new ArrayList<>();

        for (MLText.Block block : blocks) {
            List<MLText.TextLine> lines = block.getContents();
            for (MLText.TextLine line : lines) {
                String text = line.getStringValue();
                text = PostProcessingUtils.filterString(text, "[^a-zA-Z0-9\\.\\-,<\\(\\)\\s\\/]");
                Log.d(EmiratesIDCardProcess.TAG, "PassCardProcess text: " + text);
                Point[] points = line.getVertexes();
                Rect rect = new Rect(points[0].x, points[0].y, points[2].x, points[2].y);
                BlockItem item = new BlockItem(text, rect);
                originItems.add(item);
            }
        }
        return originItems;
    }
}
