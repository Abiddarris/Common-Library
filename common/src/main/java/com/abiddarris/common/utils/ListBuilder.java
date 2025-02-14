/***********************************************************************************
 * Copyright 2024-2025 Abiddarris
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ***********************************************************************************/
package com.abiddarris.common.utils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ListBuilder {

    private Map<Integer,Map<Integer,String>> data = new HashMap<>();
    private Map<Integer,Integer> maxCharOnColumn = new HashMap<>();
    private int lastRow = 0;
    private int lastColumn = -1;
    private int maxrow = -1;
    private int maxColumn = -1;

    public ListBuilder(){
    }

    public ListBuilder(int column,int row){
        this(column,row,new String[0]);
    }

    public ListBuilder(int column,int row,String[] columnNames){
        for(int i = -1; i <= column; i++){
            data.put(i,new HashMap<Integer,String>());
            maxCharOnColumn.put(i,0);
        }
        maxColumn = column;
        maxrow = row;
    }
	
	public ListBuilder addColumn(String name){
		return setColumnName(getColumn(),name);
	}

	public int getColumnIndex(String columnName){
		for(Integer i : data.keySet()){
			Map<Integer,String> columnData = data.get(i);
			String columnName0 = columnData.get(-1);
			if(columnName.equals(columnName0))
				return i;
		}
		return -1;
	}
	
	public int getColumn(){
		return maxColumn + 1;
	}
	
	public int getRow(){
		return maxrow + 1;
	}
	
	public int getCurrentRow() {
		return lastRow;
	}

	public int getCurrentColumn() {
		return lastColumn;
	}

    private boolean isEmptyString(String str){
        String[] chars = str.split("");
        for(int i = 1; i < chars.length; i++){
            if(!chars[i].equalsIgnoreCase(" ")){
                return false;
            }
        }
        return true;
    }
    private String removeSpaceOnLast(String str){
        int a = 0;
        if(str.charAt(str.length() - 1) != ' ')
            return str;

        for(int i = str.length() - 1; i >= 0; i--){
            if(str.charAt(i) == ' ')
                a = i;      
            else 
                break;      
        }
        return str.substring(0,a);
    }

    public static ListBuilder from(String result,String[] columns){
        return new ListBuilder().from0(result,columns);
    }

    private ListBuilder from0(String result,String[] columns){
        ListBuilder builder = this;

        try{        
            String[] result0 = result.split("\n");
            int[] index = new int[columns.length];

            for(int i = 0; i < columns.length; i++){
                builder.setColumnName(i,columns[i]);
                index[i] = result0[0].indexOf(columns[i]);      
            }

            Map<Integer,String> tempMap = new HashMap<>();
            int row = 0;

            for(int i = 1; i < result0.length; i++){        
                int column = 0;
                boolean rowIsDecrement = false;
                for(int j = 0; j < index.length; j++){
                    int index0 = 0;

                    if(j != index.length - 1){
                        index0 = index[j + 1];
                    } else {
                        index0 = result0[i].length();
                    }

                    if(index0 > result0[i].length()) index0 = result0[i].length();
                    int index1 = index[j];
                    if(index1 > result0[i].length()) index1 = result0[i].length();

                    String val = result0[i].substring(index1,index0);

                    if(!isEmptyString(val)){                                                
                        tempMap.put(column,removeSpaceOnLast(val));
                    } else {
                        rowIsDecrement = true;
                    }
                    column++;
                }

                if(rowIsDecrement)
                    row--;

                for(Integer key : tempMap.keySet()){
                    String val = tempMap.get(key);
                    if(rowIsDecrement){

                        String val0 = data.get(key).get(row);
                        if(val0 != null){
                            String val1 = val0 + "\n" + val;
                            val = val1;
                        }
                    }
                    add(key,row,val);
                }
                tempMap.clear();
                row++;
            }
            builder.maxrow = row - 1;

        } catch (Exception e){      
            throw new UnsupportedListBuilderOperation(e);
        }   
        return builder;
    }

    public ListBuilder nextLine(String str){
        lastColumn = 0;
        lastRow++;
        add(lastColumn,lastRow,str);
        return this;
    }

    public ListBuilder moveToFirst(String str){
        lastRow = 0;
        lastColumn = 0;
        return add(lastColumn,lastRow,str);
    }

    public ListBuilder nextLine(){
        lastColumn = 0;
        lastRow++;
        return this;
    }

    public ListBuilder moveToFirst(){
        lastRow = 0;
        lastColumn = 0;
        return this;
    }

    public ListBuilder put(String str){
        return add(lastColumn,lastRow,str);
    }

    public ListBuilder prevRow(){
        lastRow--;
        return this;
    }

    public ListBuilder prev(){
        lastColumn--;
        return this;
    }

    public ListBuilder nextRow(){
        lastRow++;
        return this;
    }

    public ListBuilder next(){
        lastColumn++;
        return this;
    }

    public ListBuilder move(int column,int row,String str){
        lastColumn += column;
        lastRow += row;
        return add(lastColumn,lastRow,str);
    }

    public ListBuilder prevRow(String str){
        lastRow--;
        if(lastRow < 0){
            lastRow = 0;
            return this;
        }
        return add(lastColumn,lastRow,str);
    }

    public ListBuilder prev(String str){
        lastColumn--;
        if(lastColumn < 0){
            lastColumn = maxColumn;
            lastRow--;
            if(lastRow < 0){
                lastRow = 0;
                return this;
            }
        }
        return add(lastColumn,lastRow,str);
    }


    public ListBuilder nextRow(String str){
        lastRow++;
        return add(lastColumn,lastRow,str);
    }

    public ListBuilder next(String str){
        lastColumn++;
        return add(lastColumn,lastRow,str);
    }

    public ListBuilder setColumnName(int column,String str){
        Map<Integer,String> a = data.get(column);
        if(a == null){
            data.put(column,new HashMap<Integer,String>());
            a = data.get(column);
        }
        a.put(-1,str);
        if(column > maxColumn) maxColumn = column;
        if(getLengthFromString(str) > (maxCharOnColumn.get(column) == null ? 0 : maxCharOnColumn.get(column))) maxCharOnColumn.put(column,getLengthFromString(str));
        return this;
    }

    public ListBuilder add(int column,int row,String str){
        Map<Integer,String> a = data.get(column);
        if(a == null){
            data.put(column,new HashMap<Integer,String>());
            a = data.get(column);
        }
        a.put(row,str);
        if(getLengthFromString(str) > (maxCharOnColumn.get(column) == null ? 0 : maxCharOnColumn.get(column))) maxCharOnColumn.put(column,getLengthFromString(str));
        lastRow = row; 
        lastColumn = column;
        if(row > maxrow) maxrow = row;
        if(column > maxColumn) maxColumn = column;
        return this;
    }

    public String getNextLine(){
        lastColumn = 0;
        lastRow++;
        return get(lastColumn,lastRow);
    }

    public String getMoveToFirst(){
        lastRow = 0;
        lastColumn = 0;
        return get(lastColumn,lastRow);
    }

    public String get(){
        return get(lastColumn,lastRow);
    }

    public String getMove(int column,int row){
        lastColumn += column;
        lastRow += row;
        return get(lastColumn,lastRow);
    }

    public String getPrevRow(){
        lastRow--;
        if(lastRow < 0){
            lastRow = 0;
            return null;
        }
        return get(lastColumn,lastRow);
    }

    public String getPrev(){
        lastColumn--;
        if(lastColumn < 0){
            lastColumn = maxColumn;
            lastRow--;
            if(lastRow < 0){
                lastRow = 0;
                return null;
            }
        }
        return get(lastColumn,lastRow);
    }


    public String getNextRow(){
        lastRow++;
        return get(lastColumn,lastRow);
    }

    public String getNext(){
        lastColumn++;
        return get(lastColumn,lastRow);
    }

    public String get(int column,int row){
        Map<Integer,String> a = data.get(column);
        if(a == null){
            data.put(column,new HashMap<Integer,String>());
            a = data.get(column);
        }
        return a.get(row);
    }

    public String build(){
        StringBuilder result = new StringBuilder();

        if(maxrow == -1) return result.toString();

        for(int row = -1; row <= maxrow; row++){
            String[] extraLine = new String[0];

            for(int column = 0; column <= maxColumn; column++){
                String val = data.get(column).get(row);
                if(val == null || val == "") val = "-";
                String[] vals = val.split("\n");
                String[] vals0 = Arrays.copyOfRange(vals,1,vals.length);
                if(vals0.length != 0){
                    if(vals0.length >= extraLine.length){
                        String[] extraLineTemp = new String[vals0.length];
                        Arrays.fill(extraLineTemp,0,extraLineTemp.length,"");
                        for(int i = 0; i < extraLine.length; i++){
                            extraLineTemp[i] = extraLine[i];
                        }                   
                        extraLine = extraLineTemp;
                    }

                    for(int i = 0; i < vals0.length; i++){
                        int space = 0;
                        for(int column0 = 0; column0 < column; column0++){
                            space += maxCharOnColumn.get(column0) + 3;
                        }


                        StringBuilder builder = new StringBuilder();
                        for(int j = 0; j < space; j++) builder.append(" ");             

                        String val0 = builder.toString() + vals0[i];
                        extraLine[i] += val0.substring(extraLine[i].length());
                    }
                }

                result.append(vals[0]);
                for(int i = 0; i < (maxCharOnColumn.get(column) + 3) - vals[0].length(); i++){
                    if(column != maxColumn)
                        result.append(" ");
                }
            }
            result.append("\n");
            if(extraLine != null){
                for(String extraLine0 : extraLine){
                    result.append(extraLine0 + "\n");

                }

            }
        }

        return result.toString();
    }

    private int getLengthFromString(String str){
        if(str == null) return 0;
        String[] a = str.split("\n");
        int maxLen = 0;
        for(String b : a){
            if(b.length() > maxLen) maxLen = b.length();
        }
        return maxLen;
    }

    @Override
    public String toString(){
        return build();
    }

    public static final class UnsupportedListBuilderOperation extends RuntimeException{
        public UnsupportedListBuilderOperation(){
            super();
        }

        public UnsupportedListBuilderOperation(String message){
            super(message);
        }

        public UnsupportedListBuilderOperation(String message, Throwable cause){
            super(message,cause);
        }

        public UnsupportedListBuilderOperation(Throwable cause){
            super(cause);
        }       
    }
}
