
function [Result]=RescaleToImageResult(E,sgrid,kx,ky,pixels)
odd_diff=E;
for x=1:kx
    for y=1:ky
        a=(x-1)*(sgrid*8)+1;
        b=(y-1)*(sgrid*8)+1;
       result(a:a+(sgrid*8-1),b:b+(sgrid*8-1))  = odd_diff(x,y);
    end
end

[xim, yim]=size(pixels);
[xres, yres]=size(result);
diffx=xim-xres;
diffy=yim-yres;
Result=zeros(xim,yim);
Result(1:xres,1:yres)=result;

for  k=xres:xres+diffx
    for y=1:yres
     Result(k, y)=result(xres,y);
    end
end

for  k=1:xim
    for y=yres:yres+diffy
     Result(k, y)=Result(k,yres);
    end
end