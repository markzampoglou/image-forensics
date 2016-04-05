function OutputMap = ELA( ImIn, Quality, Multiplier, Flatten)
    % Copyright (C) 2016 Markos Zampoglou
    % Information Technologies Institute, Centre for Research and Technology Hellas
    % 6th Km Harilaou-Thermis, Thessaloniki 57001, Greece
    %
    % This code implements the Error Level Analysis algorithm popularized
    % by http://fotoforensics.com/
    %
    % ImIn is an image loaded using imread/(CleanUpImage)
    % Quality is the quality in which to recompress the image
    % Multiplier is the value with which to multiply the residual to make
    % it more visible
    % If Flatten=true, the image is converted to grayscale using the
    % cross-channel mean
    
    
    imwrite(ImIn,'tmp.jpg','Quality',Quality);
    ImJPG=imread('tmp.jpg');
    
    OutputMap=(abs(double(ImIn)-double(ImJPG))*Multiplier);
    
    
    if Flatten
        OutputMap=mean(OutputMap,3);
    end
end

