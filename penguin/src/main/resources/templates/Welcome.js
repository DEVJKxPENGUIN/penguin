import React, { useState } from 'react';




	const YantraHomepage = () => {
	  const [message, setMessage] = useState('');
	
	  const handleClick = () => {
	    setMessage('Welcome to Yantra!');
	  };
	  
function generateain(){	
	  return (
	    <div className="relative h-screen bg-cover bg-center" style={{backgroundImage: "url('/Users/hongsohee/Downloads/2.png')"}}>
	      {/* Navigation */}
	      <nav className="flex justify-between items-center p-4 text-white">
	        <div className="text-2xl font-bold">YANTRA</div>
	        <div className="space-x-4">
	          <button className="hover:underline">Home</button>
	          <button className="hover:underline">About</button>
	          <button className="hover:underline">Menu</button>
	          <button className="hover:underline">Press</button>
	          <button className="hover:underline">Reservations</button>
	          <button className="hover:underline">The Grand Trunk</button>
	          <button className="hover:underline">e-Vouchers</button>
	          <button className="hover:underline">Contact Us</button>
	        </div>
	      </nav>
	
	      {/* Centered Button */}
	      <div className="absolute inset-0 flex flex-col items-center justify-center">
	        <button
	          onClick={handleClick}
	          className="px-6 py-3 text-white bg-green-500 rounded-full hover:bg-green-600 focus:outline-none focus:ring-2 focus:ring-green-500 focus:ring-opacity-50 text-xl"
	        >
	          Explore Yantra
	        </button>
	        {message && (
	          <p className="mt-4 text-2xl text-white bg-black bg-opacity-50 p-2 rounded">{message}</p>
	        )}
	      </div>
	
	      {/* Footer */}
	      <footer className="absolute bottom-0 w-full p-4 text-white text-sm flex justify-between">
	        <span>© Yantra (S) Pte. Ltd. 2023</span>
	        <span>Terms of Service and Privacy Policy</span>
	      </footer>
	    </div>
	  );
	};
}
export default YantraHomepage;