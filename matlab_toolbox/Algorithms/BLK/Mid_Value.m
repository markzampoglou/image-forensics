function mid=Mid_Value(a)
% This code was provided by Dr Weihai Li, whli@ustc.edu.cn, and is the
% original implementation of the algorithm described in:
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

s=size(a); s=s(2);
for i=1:s-1
    for j=i+1:s
        if (a(i)>a(j))
            temp=a(i);
            a(i)=a(j);
            a(j)=temp;
        end
    end
end
mid=a(round(s/2));
