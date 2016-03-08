clear all; close all;
BaseFolder='/media/marzampoglou/New_NTFS_Volume/markzampoglou/ImageForensics/Datasets/FirstDigit/jpg/';
Qualities=50:5:95;
c1=1; c2=9; %maybe we should skip the DC term and c1=2; c2=10; ?
ncomp=1;
digitBinsToKeep=[2 5 7];

for Quality=Qualities
    SingleListTmp=dir([BaseFolder 'Single/' num2str(Quality) '/*.jpg']);
    SingleListTmp={SingleListTmp.name};
    SingleList{(Quality-50)/5+1}=strcat([BaseFolder 'Single/' num2str(Quality) '/'], SingleListTmp);
    
    DoubleList{(Quality-50)/5+1}={};
    DoubleListDir=dir([BaseFolder 'Double/*_' num2str(Quality) '/']);
    DoubleListDir={DoubleListDir.name};
    for DoubleDir=1:length(DoubleListDir)
        DoubleListTmp=dir([BaseFolder 'Double/' DoubleListDir{DoubleDir} '/*.jpg']);
        DoubleListTmp={DoubleListTmp.name};
        DoubleListTmp=strcat([BaseFolder 'Double/' DoubleListDir{DoubleDir} '/'], DoubleListTmp);
        DoubleList{(Quality-50)/5+1}=[DoubleList{(Quality-50)/5+1} DoubleListTmp];
    end
    SingleFeatures{(Quality-50)/5+1}=zeros(length(SingleList),(c2-c1+1)*length(digitBinsToKeep));
    for SingleItem=1:length(SingleList)
        im=jpeg_read(SingleList{SingleItem});
        SingleFeatures{(Quality-50)/5+1}(SingleItem,:)=ExtractFeatures(im, c1, c2, ncomp, digitBinsToKeep);
    end
    DoubleFeatures{(Quality-50)/5+1}=zeros(length(DoubleList),(c2-c1+1)*length(digitBinsToKeep));
    for DoubleItem=1:length(DoubleList)
        im=jpeg_read(DoubleList{DoubleItem});
        DoubleFeatures{(Quality-50)/5+1}(DoubleItem,:)=ExtractFeatures(im, c1, c2, ncomp, digitBinsToKeep);
    end
    disp(Quality);
end

save('TrainingFeature.mat','SingleFeatures','DoubleFeatures','SingleList','DoubleList');