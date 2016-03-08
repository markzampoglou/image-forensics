clear all;close all;

UCIDPath='/media/marzampoglou/3TB_B/ImageForensics/Datasets/UCID/';
OutputPath='/media/marzampoglou/New_NTFS_Volume/markzampoglou/ImageForensics/Datasets/FirstDigit/Uncomp/';
mkdir(OutputPath);

UCIDImages=dir([UCIDPath '*.tif']);
UCIDImages={UCIDImages.name};

PermInd=randperm(length(UCIDImages));
ImagesToUse=UCIDImages(PermInd);

ImInd=0;
ImagesCollected=0;
while ImagesCollected<40;
    ImInd=ImInd+1;
    im=imread([UCIDPath ImagesToUse{ImInd}]);
    while ~(mod(size(im,1),64)==0 && mod(size(im,2),64)==0)
        disp('image size not divisible by 64, replacing');
        ImInd=ImInd+1;
        im=imread([UCIDPath ImagesToUse{ImInd}]);
    end
    [~,filename,~]=fileparts(ImagesToUse{ImInd});
    for X=1:64:size(im,1)
        for Y=1:64:size(im,2)
            block=im(X:X+63,Y:Y+63,:);
            imwrite(block,[OutputPath filename '_' num2str(X) '_' ,num2str(Y) '.png']);
        end
    end
    ImagesCollected=ImagesCollected+1;
end