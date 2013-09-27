package org.acodeeditor.util;


public class Base64 {
	private static final char[] Base64CharSet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".toCharArray();
	private static final int[] C642int = new int[256];
	static {
		for(int i=0;i<C642int.length;i++)
			C642int[i]=-1;
		for (int i = 0; i < Base64CharSet.length; i++)
			C642int[Base64CharSet[i]] = i<<2;
	}
	static boolean is64(char c){
		return C642int[c & 0xff]>=0;
	}

	static int decodeChar(char c){
		return C642int[c & 0xff];
	}
	
	static int decodeSize(String data){
		final int l=data.length();
		int s=0;
		int eque=0;
		for(int i=0;i<l;i++){
			final char c=data.charAt(i);
			if(is64(c)) s++;
			else if(c=='=') eque++;
		}
		int size=(s+eque)/4*3-eque;
		return size;
	}
	
	static void setData(byte []data,int pos,char c){
		int ibyte=pos/8;
		int ides=pos%8;
		int dc=decodeChar(c);
		if(ibyte>=data.length)return;
		data[ibyte] |= dc>>ides;
		if(ides>2 && ibyte+1 < data.length){
			data[ibyte+1] |= (byte)(dc<<(8-ides));
		}
	}
	
	public static byte[] decode(String data){
		final int size=decodeSize(data);
		if(size==0) return null;
		final byte[] res = new byte[size];
		final int l=data.length();
		int pos=0;
		for(int i=0; i<l;i++){
			char c=data.charAt(i);
			if(is64(c)){
				setData(res, pos, c);
				pos+=6;
			}
		}
		return res;
	}
}