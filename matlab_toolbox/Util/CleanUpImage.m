function [ im_out ] = CleanUpImage( filename )
% This function serves as a replacement for imread(), covering many extreme
% cases that occasionally appear in real-world datasets. This includes images 
% with other than three channels and uint16 images. These images are all
% converted to 3-channel uint8 images.

im=imread(filename);

if numel(size(im))>3
    im=im(:,:,:,1,1,1,1);
end

dots=strfind(filename,'.');
extension=filename(dots(end):end);

if strcmpi(extension,'.gif') && size(im,3)<3
    [im_gif,gif_map] =imread(filename);
    im_gif=im_gif(:,:,:,1);
    im=uint8(ind2rgb(im_gif,gif_map)*255);
end

if size(im,3)<3
    im(:,:,2)=im(:,:,1);
    im(:,:,3)=im(:,:,1);
end


if size(im,3)>3
    im=im(:,:,1:3);
end

if isa(im,'uint16')
    im=uint8(floor(double(im)/256));
end

im_out=im;

end

