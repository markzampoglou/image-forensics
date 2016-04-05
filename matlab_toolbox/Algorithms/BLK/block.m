function [doctored, pro]=block(image)
% This code, and all m-files called by it, was provided by 
% Dr Weihai Li, whli@ustc.edu.cn, and is the original implementation
% of the algorithm described in:
% Li, Weihai, Yuan Yuan, and Nenghai Yu. "Passive detection of doctored JPEG 
% image via block artifact grid extraction." Signal Processing 89, no. 9 (2009): 
% 1821-1829.
%
% This code is left here as legacy and reference to the original authors.
% We have written a different implementation using this code as a guide.
% Without loss of performance, we have significantly reduced running 
% times. Our implementation is in GetBlockGrid.m and was the one used in
% our evaluations.
%
% If you use this code please cite the aforementioned paper and notify both us
% (markzampoglou@iti.gr) and the original author

MACROBLOCK_W=16;
MACROBLOCK_H=16;
EDGE=50;

RGB=double(image);
[height,width,color]=size(RGB);
if (color==3)
    YCbCr=rgb2ycbcr(RGB);
    pixels=YCbCr(:,:,1);
else
    pixels=RGB;
end
pro=zeros(height,width);

ht=2*pixels-[pixels(:,2:width),pixels(:,width)]-[pixels(:,1),pixels(:,1:width-1)];
vt=2*pixels-[pixels(2:height,:);pixels(height,:)]-[pixels(1,:);pixels(1:height-1,:)];
%ȥ�����EDGE�ģ���Ϊ�Ǳ߽�
m=sign(1-sign(abs(ht)-EDGE)); ht=ht.*m;
m=sign(1-sign(abs(vt)-EDGE)); vt=vt.*m;

row=W_Filter(ht,MACROBLOCK_W,MACROBLOCK_H);
%m=sign(sign(row+1)); row=row.*m;
col=W_Filter(vt',MACROBLOCK_W,MACROBLOCK_H);
%m=sign(sign(col+1)); col=col.*m;
bag=row+col';
%imtool(bag*10000);

pro=Mark(bag);
%imtool(pro*10000);

doctored=bag;

%HSV=ones(height,width,3)*1;
%HSV(:,:,1)=0.67*(1-pro/(1+max(max(pro))));
%RGB=uint8(round(hsv2rgb(HSV)*255));
%doctored=RGB;
