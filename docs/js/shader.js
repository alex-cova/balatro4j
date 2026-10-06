const vertexShaderSource = `
            attribute vec4 position;
            void main() {
                gl_Position = position;
            }
        `;

const fragmentShaderSource = `
            precision highp float;
            uniform vec2 iResolution;
            uniform float iTime;

            #define SPIN_ROTATION -7.0
            #define SPIN_SPEED 4.0
            #define OFFSET vec2(0.0)
            #define COLOUR_1 vec4(0.871, 0.267, 0.231, 1.0)
            #define COLOUR_2 vec4(0.0, 0.42, 0.706, 1.0)
            #define COLOUR_3 vec4(0.086, 0.137, 0.145, 1.0)
            #define CONTRAST 3.5
            #define LIGTHING 0.4
            #define SPIN_AMOUNT 0.35
            #define PIXEL_FILTER 745.0
            #define SPIN_EASE 1.0
            #define PI 3.14159265359
            #define IS_ROTATE true

            vec4 effect(vec2 screenSize, vec2 screen_coords) {
                float pixel_size = length(screenSize.xy) / PIXEL_FILTER;
                vec2 uv = (floor(screen_coords.xy*(1./pixel_size))*pixel_size - 0.5*screenSize.xy)/length(screenSize.xy) - OFFSET;
                float uv_len = length(uv);
                
                float speed = (SPIN_ROTATION*SPIN_EASE*0.2);
                if(IS_ROTATE){
                   speed = iTime * 0.1;
                }
                speed += 302.2;
                float new_pixel_angle = atan(uv.y, uv.x) + speed - SPIN_EASE*20.*(1.*SPIN_AMOUNT*uv_len + (1. - 1.*SPIN_AMOUNT));
                vec2 mid = (screenSize.xy/length(screenSize.xy))/2.;
                uv = (vec2((uv_len * cos(new_pixel_angle) + mid.x), (uv_len * sin(new_pixel_angle) + mid.y)) - mid);
                
                uv *= 30.;
                speed = iTime*(SPIN_SPEED);
                vec2 uv2 = vec2(uv.x+uv.y);
                
                for(int i=0; i < 5; i++) {
                    uv2 += sin(max(uv.x, uv.y)) + uv;
                    uv  += 0.5*vec2(cos(5.1123314 + 0.353*uv2.y + speed*0.131121),sin(uv2.x - 0.113*speed));
                    uv  -= 1.0*cos(uv.x + uv.y) - 1.0*sin(uv.x*0.711 - uv.y);
                }
                
                float contrast_mod = (0.25*CONTRAST + 0.5*SPIN_AMOUNT + 1.2);
                float paint_res = min(2., max(0.,length(uv)*(0.035)*contrast_mod));
                float c1p = max(0.,1. - contrast_mod*abs(1.-paint_res));
                float c2p = max(0.,1. - contrast_mod*abs(paint_res));
                float c3p = 1. - min(1., c1p + c2p);
                float light = (LIGTHING - 0.2)*max(c1p*5. - 4., 0.) + LIGTHING*max(c2p*5. - 4., 0.);
                return (0.3/CONTRAST)*COLOUR_1 + (1. - 0.3/CONTRAST)*(COLOUR_1*c1p + COLOUR_2*c2p + vec4(c3p*COLOUR_3.rgb, c3p*COLOUR_1.a)) + light;
            }

            void main() {
                vec2 uv = gl_FragCoord.xy;
                gl_FragColor = effect(iResolution.xy, uv);
            }
        `;

let gl;
let program;
let positionLocation;
let timeLocation;
let resolutionLocation;
let startTime;
let animationFrameId;

function createShader(gl, type, source) {
	const shader = gl.createShader(type);
	gl.shaderSource(shader, source);
	gl.compileShader(shader);

	if (!gl.getShaderParameter(shader, gl.COMPILE_STATUS)) {
		console.error('Shader compile error:', gl.getShaderInfoLog(shader));
		gl.deleteShader(shader);
		return null;
	}
	return shader;
}

function createProgram(gl, vertexShader, fragmentShader) {
	const program = gl.createProgram();
	gl.attachShader(program, vertexShader);
	gl.attachShader(program, fragmentShader);
	gl.linkProgram(program);

	if (!gl.getProgramParameter(program, gl.LINK_STATUS)) {
		console.error('Program link error:', gl.getProgramInfoLog(program));
		return null;
	}
	return program;
}

function init() {
	const canvas = document.getElementById('glCanvas');
	if (!canvas) {
		console.error('Missing #glCanvas element for shader background');
		return;
	}

	gl = canvas.getContext('webgl');

	if (!gl) {
		console.error('WebGL not supported');
		return;
	}

	const vertexShader = createShader(gl, gl.VERTEX_SHADER, vertexShaderSource);
	const fragmentShader = createShader(gl, gl.FRAGMENT_SHADER, fragmentShaderSource);
	if (!vertexShader || !fragmentShader) {
		return;
	}

	program = createProgram(gl, vertexShader, fragmentShader);
	if (!program) {
		return;
	}

	positionLocation = gl.getAttribLocation(program, 'position');
	timeLocation = gl.getUniformLocation(program, 'iTime');
	resolutionLocation = gl.getUniformLocation(program, 'iResolution');

	const positionBuffer = gl.createBuffer();
	gl.bindBuffer(gl.ARRAY_BUFFER, positionBuffer);
	gl.bufferData(gl.ARRAY_BUFFER, new Float32Array([
		-1, -1,
		1, -1,
		-1, 1,
		-1, 1,
		1, -1,
		1, 1,
	]), gl.STATIC_DRAW);

	startTime = Date.now();
	resize();
	render();
}

function resize() {
	if (!gl) {
		return;
	}

	const canvas = gl.canvas;
	const displayWidth = canvas.clientWidth;
	const displayHeight = canvas.clientHeight;

	if (canvas.width !== displayWidth || canvas.height !== displayHeight) {
		canvas.width = displayWidth;
		canvas.height = displayHeight;
		gl.viewport(0, 0, canvas.width, canvas.height);
	}
}

function render() {
	if (!gl || !program) {
		return;
	}

	resize();

	gl.useProgram(program);
	gl.enableVertexAttribArray(positionLocation);
	gl.vertexAttribPointer(positionLocation, 2, gl.FLOAT, false, 0, 0);

	gl.uniform1f(timeLocation, (Date.now() - startTime) * 0.001);
	gl.uniform2f(resolutionLocation, gl.canvas.width, gl.canvas.height);

	gl.drawArrays(gl.TRIANGLES, 0, 6);
	animationFrameId = requestAnimationFrame(render);
}

window.addEventListener('load', init, { once: true });
window.addEventListener('resize', resize);
window.addEventListener('beforeunload', () => {
	if (animationFrameId) {
		cancelAnimationFrame(animationFrameId);
	}
});