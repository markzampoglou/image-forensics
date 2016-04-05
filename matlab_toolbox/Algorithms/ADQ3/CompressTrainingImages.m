% This script has been used to create the training image dataset for
% Amerini, Irene, Rudy Becarelli, Roberto Caldelli, and Andrea Del Mastio.
% "Splicing forgeries localization through the use of first digit features."
% In Information Forensics and Security (WIFS), 2014 IEEE International
% Workshop on, pp. 143-148. IEEE, 2014.
%
% Copyright (C) 2016 Markos Zampoglou
% Information Technologies Institute, Centre for Research and Technology Hellas
% 6th Km Harilaou-Thermis, Thessaloniki 57001, Greece

OutputPath='/media/marzampoglou/New_NTFS_Volume/markzampoglou/ImageForensics/Datasets/FirstDigit/Uncomp/';

CompressedPath='/media/marzampoglou/New_NTFS_Volume/markzampoglou/ImageForensics/Datasets/FirstDigit/jpg/';


InputList=dir([OutputPath '*.png']);
InputList={InputList.name};

Qualities=50:5:95;
for Quality=Qualities
    SingleCompPath=[CompressedPath 'Single/' num2str(Quality) '/'];
    mkdir(SingleCompPath);
    for Quality2=Qualities
        if Quality2~=Quality
            DoubleCompPath=[CompressedPath 'Double/' num2str(Quality) '_' num2str(Quality2) '/'];
            mkdir(DoubleCompPath);
        end
    end
    for firstInd=1:length(InputList)
        im=imread([OutputPath InputList{firstInd}]);
        [~,namepart,~]=fileparts(InputList{firstInd});
        imwrite(im,[SingleCompPath namepart '.jpg'],'JPG','Quality',Quality);
        SingleCompIm=imread([SingleCompPath namepart '.jpg']);
        for Quality2=Qualities
            if Quality2~=Quality
                DoubleCompPath=[CompressedPath 'Double/' num2str(Quality) '_' num2str(Quality2) '/'];
                imwrite(SingleCompIm,[DoubleCompPath namepart '.jpg'],'JPG','Quality',Quality2);
            end
        end
    end    
end