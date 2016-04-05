function res=W_Filter(ht,MACROBLOCK_W,MACROBLOCK_H)
% This code, and all m-files called by it, was provided by 
% Dr Weihai Li, whli@ustc.edu.cn, and is the original implementation
% of the algorithm described in:
% Li, Weihai, Yuan Yuan, and Nenghai Yu. "Passive detection of doctored JPEG 
% image via block artifact grid extraction." Signal Processing 89, no. 9 (2009): 
% 1821-1829.
%
% We have written a different implementation using this code as a
% guide. Without loss of performance, we have significantly reduced running
% times. Our implementation is in GetBlockGrid.m
%
% If you use this code please cite the aforementioned paper and notify both us
% (markzampoglou@iti.gr) and the original author

NH=MACROBLOCK_H*2+1; NW=MACROBLOCK_W*2+1;
[height,width]=size(ht);
pro=zeros(height,width);

hta=abs(ht);
ht_s=ht;
%����������ȡ��var()
a=sum(hta(1:MACROBLOCK_H*2+1,:));
ht_s(MACROBLOCK_H+1,:)=a;
for i=MACROBLOCK_H+2:height-MACROBLOCK_H
    a=a-hta(i-MACROBLOCK_H-1,:)+hta(i+MACROBLOCK_H,:);
    ht_s(i,:)=a;
end
ht_s(1:MACROBLOCK_H,:)=ones(MACROBLOCK_H,1)*ht_s(MACROBLOCK_H+1,:);
ht_s(height-MACROBLOCK_H+1:height,:)=ones(MACROBLOCK_H,1)*ht_s(height-MACROBLOCK_H,:);
clear hta;

for i=1:height
    %����,ÿ���ȥ��������ֵ
    for j=1:width
        %ѡȡһ��a:[j-MACROBLOCK_W,j+MACROBLOCK]
        left=j-MACROBLOCK_W; right=j+MACROBLOCK_W;
        if (left<1)
            left=1;
        end
        if (right>width)
            right=width;
        end
        k=j-left+1;
        %�����ֵs������a-s
        a=ht_s(i,left:right);
        s=Mid_Value(a);
        ht_sf(i,j)=a(k)-s;
    end
    %����,��8Ϊ�����˲�
    for j=1:width
        %ѡȡһ��a:[j-MACROBLOCK_W,j+MACROBLOCK]
        left=j-MACROBLOCK_W; right=j+MACROBLOCK_W;
        if (left<1)
            left=1;
        end
        if (right>width)
            right=width;
        end
        k=j-left+1;
        a=ht_sf(i,left:right);
        c=[a(8-mod(8-k,8):8:right-left+1)];
        pick=Mid_Value(c);
%        if (pick<0)% || mod(i,8)==0 || mod(j,8)==0)
%            pick=0;
%        end
        pro(i,j)=pick;
    end
end

res=pro;
