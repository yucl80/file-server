var file = [], p = true;
//window.BlobBuilder = window.MozBlobBuilder || window.WebKitBlobBuilder ||  window.BlobBuilder;
function upload(fileId, blobOrFile, start, size) {
    var xhr = new XMLHttpRequest();
    xhr.open('POST', '/uploadPartFile', false);
    xhr.onload = function (e) {
    };
    var fd = new FormData();
    fd.append("file", blobOrFile);
    xhr.setRequestHeader("Content-Range", "bytes " + start + "-" + (start + blobOrFile.size) + "/" + size);
    xhr.setRequestHeader("Content-Disposition", "attachment; filename=" + fileId);
    //xhr.send(fd)
    xhr.send(blobOrFile);
}

function getFileId(file) {
    var xhr = new XMLHttpRequest();
    xhr.open('POST', '/newFile/ae', false);
    xhr.onload = function (e) {
    };
    var  fileId = null;
    var fd = new FormData();
    fd.append("fileName", file.name);
    fd.append("fileLength", file.size);
    fd.append("fileType",file.type);
    xhr.send(fd)
    console.log(xhr.responseText);
    fileId =JSON.parse(xhr.responseText)["FileId"];
    return fileId;
}

function process() {
    for (var j = 0; j < file.length; j++) {
        var blob = file[j];
        const BYTES_PER_CHUNK = 1024 * 1024;
        const SIZE = blob.size;
        var fileId = getFileId(blob);
        var start = 0;
        var end = BYTES_PER_CHUNK;
        while (start < SIZE) {
            if ('mozSlice' in blob) {
                var chunk = blob.mozSlice(start, end);
            } else if ('webkitSlice' in blob) {
                var chunk = blob.webkitSlice(start, end);
            } else {
                var chunk = blob.slice(start, end);
            }
            upload(fileId, chunk, start, SIZE);
            start = end;
            end = start + BYTES_PER_CHUNK;
        }
        p = ( j = file.length - 1) ? true : false;
        self.postMessage(blob.name + " Uploaded Succesfully");
    }
}


self.onmessage = function (e) {
    for (var j = 0; j < e.data.files.length; j++)
        file.push(e.data.files[j]);
    if (p) {
        process()
    }
}
